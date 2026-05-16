package com.subscription.billing.billing.dto;

import com.subscription.billing.billing.BillingInterval;
import com.subscription.billing.billing.Plan;
import java.time.Instant;

public record PlanResponse(
        Long id,
        String name,
        Integer priceCents,
        String currency,
        BillingInterval billingInterval,
        Integer trialDays,
        boolean active,
        Instant createdAt
) {
    public static PlanResponse from(Plan plan) {
        return new PlanResponse(
                plan.getId(),
                plan.getName(),
                plan.getPriceCents(),
                plan.getCurrency(),
                plan.getBillingInterval(),
                plan.getTrialDays(),
                plan.isActive(),
                plan.getCreatedAt()
        );
    }
}
