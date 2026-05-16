package com.subscription.billing.billing;

import com.subscription.billing.billing.dto.BillingStatsResponse;
import com.subscription.billing.billing.dto.CustomerRequest;
import com.subscription.billing.billing.dto.CustomerResponse;
import com.subscription.billing.billing.dto.InvoiceResponse;
import com.subscription.billing.billing.dto.PlanRequest;
import com.subscription.billing.billing.dto.PlanResponse;
import com.subscription.billing.billing.dto.SubscriptionRequest;
import com.subscription.billing.billing.dto.SubscriptionResponse;
import com.subscription.billing.users.User;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class BillingController {

    private final BillingService billingService;

    public BillingController(BillingService billingService) {
        this.billingService = billingService;
    }

    @GetMapping("/plans")
    public List<PlanResponse> listPlans(@AuthenticationPrincipal User user) {
        return billingService.listPlans(user).stream().map(PlanResponse::from).toList();
    }

    @PostMapping("/plans")
    @ResponseStatus(HttpStatus.CREATED)
    public PlanResponse createPlan(@AuthenticationPrincipal User user, @Valid @RequestBody PlanRequest request) {
        return PlanResponse.from(billingService.createPlan(user, request));
    }

    @PatchMapping("/plans/{planId}/deactivate")
    public PlanResponse deactivatePlan(@AuthenticationPrincipal User user, @PathVariable Long planId) {
        return PlanResponse.from(billingService.deactivatePlan(user, planId));
    }

    @GetMapping("/customers")
    public List<CustomerResponse> listCustomers(@AuthenticationPrincipal User user) {
        return billingService.listCustomers(user).stream().map(CustomerResponse::from).toList();
    }

    @PostMapping("/customers")
    @ResponseStatus(HttpStatus.CREATED)
    public CustomerResponse createCustomer(@AuthenticationPrincipal User user, @Valid @RequestBody CustomerRequest request) {
        return CustomerResponse.from(billingService.createCustomer(user, request));
    }

    @GetMapping("/subscriptions")
    public List<SubscriptionResponse> listSubscriptions(@AuthenticationPrincipal User user) {
        return billingService.listSubscriptions(user).stream().map(SubscriptionResponse::from).toList();
    }

    @PostMapping("/subscriptions")
    @ResponseStatus(HttpStatus.CREATED)
    public SubscriptionResponse createSubscription(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody SubscriptionRequest request
    ) {
        return SubscriptionResponse.from(billingService.createSubscription(user, request));
    }

    @PatchMapping("/subscriptions/{subscriptionId}/cancel")
    public SubscriptionResponse cancelSubscription(@AuthenticationPrincipal User user, @PathVariable Long subscriptionId) {
        return SubscriptionResponse.from(billingService.cancelSubscription(user, subscriptionId));
    }

    @GetMapping("/invoices")
    public List<InvoiceResponse> listInvoices(@AuthenticationPrincipal User user) {
        return billingService.listInvoices(user).stream().map(InvoiceResponse::from).toList();
    }

    @PatchMapping("/invoices/{invoiceId}/pay")
    public InvoiceResponse payInvoice(@AuthenticationPrincipal User user, @PathVariable Long invoiceId) {
        return InvoiceResponse.from(billingService.payInvoice(user, invoiceId));
    }

    @GetMapping("/billing/stats")
    public BillingStatsResponse stats(@AuthenticationPrincipal User user) {
        return billingService.stats(user);
    }
}
