package com.subscription.billing.billing.dto;

import com.subscription.billing.billing.Subscription;
import com.subscription.billing.billing.SubscriptionStatus;
import java.time.Instant;
import java.time.LocalDate;

public record SubscriptionResponse(
        Long id,
        Long customerId,
        String customerEmail,
        Long planId,
        String planName,
        SubscriptionStatus status,
        LocalDate currentPeriodStart,
        LocalDate currentPeriodEnd,
        Instant canceledAt,
        Instant createdAt
) {
    public static SubscriptionResponse from(Subscription subscription) {
        return new SubscriptionResponse(
                subscription.getId(),
                subscription.getCustomer().getId(),
                subscription.getCustomer().getEmail(),
                subscription.getPlan().getId(),
                subscription.getPlan().getName(),
                subscription.getStatus(),
                subscription.getCurrentPeriodStart(),
                subscription.getCurrentPeriodEnd(),
                subscription.getCanceledAt(),
                subscription.getCreatedAt()
        );
    }
}
