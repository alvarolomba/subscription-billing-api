package com.subscription.billing;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Testcontainers
class SubscriptionBillingApplicationTests {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("billing")
            .withUsername("billing")
            .withPassword("billing");

    @Autowired
    private MockMvc mockMvc;

    @DynamicPropertySource
    static void configurePostgres(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Test
    void userCanRegisterLoginAndReadCurrentProfile() throws Exception {
        register("profile@example.com");

        String token = login("profile@example.com");

        mockMvc.perform(get("/auth/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("profile@example.com"))
                .andExpect(jsonPath("$.fullName").value("Ana Backend"));
    }

    @Test
    void duplicateEmailIsRejected() throws Exception {
        register("duplicate@example.com");

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "duplicate@example.com",
                                  "fullName": "Ana Backend",
                                  "password": "strong-password"
                                }
                                """))
                .andExpect(status().isConflict());
    }

    @Test
    void userCanCreateSubscriptionPayInvoiceAndReadStats() throws Exception {
        register("billing@example.com");
        String token = login("billing@example.com");

        int planId = createPlan(token, "Pro", 2900);
        int customerId = createCustomer(token, "buyer@example.com");

        MvcResult subscriptionResult = mockMvc.perform(post("/subscriptions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": %d,
                                  "planId": %d
                                }
                                """.formatted(customerId, planId)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("TRIALING"))
                .andExpect(jsonPath("$.planName").value("Pro"))
                .andReturn();

        int subscriptionId = JsonPath.read(subscriptionResult.getResponse().getContentAsString(), "$.id");

        MvcResult invoicesResult = mockMvc.perform(get("/invoices")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].amountCents").value(2900))
                .andExpect(jsonPath("$[0].status").value("OPEN"))
                .andReturn();

        int invoiceId = JsonPath.read(invoicesResult.getResponse().getContentAsString(), "$[0].id");

        mockMvc.perform(patch("/invoices/" + invoiceId + "/pay")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));

        mockMvc.perform(get("/billing/stats").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.trialingSubscriptions").value(1))
                .andExpect(jsonPath("$.openInvoices").value(0))
                .andExpect(jsonPath("$.monthlyRecurringRevenueCents").value(2900))
                .andExpect(jsonPath("$.paidRevenueCents").value(2900));

        mockMvc.perform(patch("/subscriptions/" + subscriptionId + "/cancel")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELED"));
    }

    @Test
    void usersCannotAccessEachOthersBillingData() throws Exception {
        register("ana@example.com");
        register("bob@example.com");
        String anaToken = login("ana@example.com");
        String bobToken = login("bob@example.com");

        int planId = createPlan(anaToken, "Team", 9900);
        int customerId = createCustomer(anaToken, "team@example.com");

        mockMvc.perform(post("/subscriptions")
                        .header("Authorization", "Bearer " + bobToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": %d,
                                  "planId": %d
                                }
                        """.formatted(customerId, planId)))
                .andExpect(status().isNotFound());
    }

    @Test
    void inactivePlanCannotBeSubscribedTo() throws Exception {
        register("inactive-plan@example.com");
        String token = login("inactive-plan@example.com");

        int planId = createPlan(token, "Legacy", 4900);
        int customerId = createCustomer(token, "legacy-buyer@example.com");

        mockMvc.perform(patch("/plans/" + planId + "/deactivate")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.active").value(false));

        mockMvc.perform(post("/subscriptions")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": %d,
                                  "planId": %d
                                }
                                """.formatted(customerId, planId)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void authenticationIsRequired() throws Exception {
        mockMvc.perform(get("/plans"))
                .andExpect(status().isUnauthorized());
    }

    private int createPlan(String token, String name, int priceCents) throws Exception {
        MvcResult result = mockMvc.perform(post("/plans")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "%s",
                                  "priceCents": %d,
                                  "currency": "USD",
                                  "billingInterval": "MONTHLY",
                                  "trialDays": 14
                                }
                                """.formatted(name, priceCents)))
                .andExpect(status().isCreated())
                .andReturn();

        return JsonPath.read(result.getResponse().getContentAsString(), "$.id");
    }

    private int createCustomer(String token, String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/customers")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "name": "Buyer One",
                                  "company": "Acme Inc"
                                }
                                """.formatted(email)))
                .andExpect(status().isCreated())
                .andReturn();

        return JsonPath.read(result.getResponse().getContentAsString(), "$.id");
    }

    private void register(String email) throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "fullName": "Ana Backend",
                                  "password": "strong-password"
                                }
                                """.formatted(email)))
                .andExpect(status().isCreated());
    }

    private String login(String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "%s",
                                  "password": "strong-password"
                                }
                                """.formatted(email)))
                .andExpect(status().isOk())
                .andReturn();

        return JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");
    }
}
