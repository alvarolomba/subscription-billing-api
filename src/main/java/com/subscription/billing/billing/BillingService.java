package com.subscription.billing.billing;

import com.subscription.billing.billing.dto.BillingStatsResponse;
import com.subscription.billing.billing.dto.CustomerRequest;
import com.subscription.billing.billing.dto.PlanRequest;
import com.subscription.billing.billing.dto.SubscriptionRequest;
import com.subscription.billing.users.User;
import java.time.LocalDate;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BillingService {

    private final PlanRepository planRepository;
    private final CustomerRepository customerRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final InvoiceRepository invoiceRepository;

    public BillingService(
            PlanRepository planRepository,
            CustomerRepository customerRepository,
            SubscriptionRepository subscriptionRepository,
            InvoiceRepository invoiceRepository
    ) {
        this.planRepository = planRepository;
        this.customerRepository = customerRepository;
        this.subscriptionRepository = subscriptionRepository;
        this.invoiceRepository = invoiceRepository;
    }

    @Transactional(readOnly = true)
    public List<Plan> listPlans(User owner) {
        return planRepository.findByOwnerIdOrderByCreatedAtDesc(owner.getId());
    }

    @Transactional
    public Plan createPlan(User owner, PlanRequest request) {
        return planRepository.save(new Plan(
                owner,
                request.name(),
                request.priceCents(),
                request.currency(),
                request.billingInterval(),
                request.trialDays()
        ));
    }

    @Transactional
    public Plan deactivatePlan(User owner, Long planId) {
        Plan plan = getPlan(owner, planId);
        plan.deactivate();
        return planRepository.save(plan);
    }

    @Transactional(readOnly = true)
    public List<Customer> listCustomers(User owner) {
        return customerRepository.findByOwnerIdOrderByCreatedAtDesc(owner.getId());
    }

    @Transactional
    public Customer createCustomer(User owner, CustomerRequest request) {
        String email = request.email().toLowerCase();
        if (customerRepository.existsByOwnerIdAndEmail(owner.getId(), email)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Customer email already exists");
        }
        return customerRepository.save(new Customer(owner, email, request.name(), request.company()));
    }

    @Transactional(readOnly = true)
    public List<Subscription> listSubscriptions(User owner) {
        return subscriptionRepository.findByOwnerIdOrderByCreatedAtDesc(owner.getId());
    }

    @Transactional
    public Subscription createSubscription(User owner, SubscriptionRequest request) {
        Customer customer = getCustomer(owner, request.customerId());
        Plan plan = getPlan(owner, request.planId());
        if (!plan.isActive()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot subscribe to an inactive plan");
        }

        LocalDate start = LocalDate.now();
        LocalDate end = plan.getBillingInterval() == BillingInterval.YEARLY ? start.plusYears(1) : start.plusMonths(1);
        SubscriptionStatus status = plan.getTrialDays() > 0 ? SubscriptionStatus.TRIALING : SubscriptionStatus.ACTIVE;
        Subscription subscription = subscriptionRepository.save(new Subscription(owner, customer, plan, status, start, end));

        invoiceRepository.save(new Invoice(
                owner,
                customer,
                subscription,
                plan.getPriceCents(),
                plan.getCurrency(),
                start.plusDays(14)
        ));

        return subscription;
    }

    @Transactional
    public Subscription cancelSubscription(User owner, Long subscriptionId) {
        Subscription subscription = getSubscription(owner, subscriptionId);
        if (subscription.getStatus() == SubscriptionStatus.CANCELED) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Subscription is already canceled");
        }
        subscription.cancel();
        return subscriptionRepository.save(subscription);
    }

    @Transactional(readOnly = true)
    public List<Invoice> listInvoices(User owner) {
        return invoiceRepository.findByOwnerIdOrderByCreatedAtDesc(owner.getId());
    }

    @Transactional
    public Invoice payInvoice(User owner, Long invoiceId) {
        Invoice invoice = invoiceRepository.findByIdAndOwnerId(invoiceId, owner.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invoice not found"));
        if (invoice.getStatus() != InvoiceStatus.OPEN) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Only open invoices can be paid");
        }
        invoice.markPaid();
        return invoiceRepository.save(invoice);
    }

    @Transactional(readOnly = true)
    public BillingStatsResponse stats(User owner) {
        Long ownerId = owner.getId();
        return new BillingStatsResponse(
                subscriptionRepository.countByOwnerIdAndStatus(ownerId, SubscriptionStatus.ACTIVE),
                subscriptionRepository.countByOwnerIdAndStatus(ownerId, SubscriptionStatus.TRIALING),
                invoiceRepository.countByOwnerIdAndStatus(ownerId, InvoiceStatus.OPEN),
                calculateMonthlyRecurringRevenue(owner),
                invoiceRepository.sumAmountByOwnerIdAndStatus(ownerId, InvoiceStatus.PAID)
        );
    }

    private long calculateMonthlyRecurringRevenue(User owner) {
        return subscriptionRepository.findByOwnerIdOrderByCreatedAtDesc(owner.getId())
                .stream()
                .filter(subscription -> subscription.getStatus() == SubscriptionStatus.ACTIVE
                        || subscription.getStatus() == SubscriptionStatus.TRIALING)
                .mapToLong(subscription -> {
                    int price = subscription.getPlan().getPriceCents();
                    return subscription.getPlan().getBillingInterval() == BillingInterval.YEARLY ? price / 12 : price;
                })
                .sum();
    }

    private Plan getPlan(User owner, Long planId) {
        return planRepository.findByIdAndOwnerId(planId, owner.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Plan not found"));
    }

    private Customer getCustomer(User owner, Long customerId) {
        return customerRepository.findByIdAndOwnerId(customerId, owner.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Customer not found"));
    }

    private Subscription getSubscription(User owner, Long subscriptionId) {
        return subscriptionRepository.findByIdAndOwnerId(subscriptionId, owner.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Subscription not found"));
    }
}
