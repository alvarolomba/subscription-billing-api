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

@Entity
@Table(name = "plans")
public class Plan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false)
    private Integer priceCents;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BillingInterval billingInterval;

    @Column(nullable = false)
    private Integer trialDays;

    @Column(nullable = false)
    private boolean active = true;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Plan() {
    }

    public Plan(User owner, String name, Integer priceCents, String currency, BillingInterval billingInterval, Integer trialDays) {
        this.owner = owner;
        this.name = name;
        this.priceCents = priceCents;
        this.currency = currency.toUpperCase();
        this.billingInterval = billingInterval;
        this.trialDays = trialDays;
    }

    public Long getId() {
        return id;
    }

    public User getOwner() {
        return owner;
    }

    public String getName() {
        return name;
    }

    public Integer getPriceCents() {
        return priceCents;
    }

    public String getCurrency() {
        return currency;
    }

    public BillingInterval getBillingInterval() {
        return billingInterval;
    }

    public Integer getTrialDays() {
        return trialDays;
    }

    public boolean isActive() {
        return active;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void deactivate() {
        this.active = false;
    }
}
