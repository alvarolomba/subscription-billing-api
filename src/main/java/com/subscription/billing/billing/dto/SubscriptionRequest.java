package com.subscription.billing.billing.dto;

import jakarta.validation.constraints.NotNull;

public record SubscriptionRequest(
        @NotNull Long customerId,
        @NotNull Long planId
) {
}
