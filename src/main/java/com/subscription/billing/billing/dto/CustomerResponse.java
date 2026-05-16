package com.subscription.billing.billing.dto;

import com.subscription.billing.billing.Customer;
import java.time.Instant;

public record CustomerResponse(
        Long id,
        String email,
        String name,
        String company,
        Instant createdAt
) {
    public static CustomerResponse from(Customer customer) {
        return new CustomerResponse(
                customer.getId(),
                customer.getEmail(),
                customer.getName(),
                customer.getCompany(),
                customer.getCreatedAt()
        );
    }
}
