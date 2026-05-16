package com.subscription.billing.billing;

import com.subscription.billing.users.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "invoices")
public class Invoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    @Column(nullable = false)
    private Integer amountCents;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InvoiceStatus status = InvoiceStatus.OPEN;

    @Column(nullable = false)
    private LocalDate dueDate;

    private Instant paidAt;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Invoice() {
    }

    public Invoice(User owner, Customer customer, Subscription subscription, Integer amountCents, String currency, LocalDate dueDate) {
        this.owner = owner;
        this.customer = customer;
        this.subscription = subscription;
        this.amountCents = amountCents;
        this.currency = currency;
        this.dueDate = dueDate;
    }

    public Long getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public Subscription getSubscription() {
        return subscription;
    }

    public Integer getAmountCents() {
        return amountCents;
    }

    public String getCurrency() {
        return currency;
    }

    public InvoiceStatus getStatus() {
        return status;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void markPaid() {
        this.status = InvoiceStatus.PAID;
        this.paidAt = Instant.now();
    }
}
