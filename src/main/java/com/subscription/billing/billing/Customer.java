package com.subscription.billing.billing;

import com.subscription.billing.users.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(nullable = false, length = 255)
    private String email;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(length = 120)
    private String company;

    @Column(nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    protected Customer() {
    }

    public Customer(User owner, String email, String name, String company) {
        this.owner = owner;
        this.email = email.toLowerCase();
        this.name = name;
        this.company = company;
    }

    public Long getId() {
        return id;
    }

    public User getOwner() {
        return owner;
    }

    public String getEmail() {
        return email;
    }

    public String getName() {
        return name;
    }

    public String getCompany() {
        return company;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
