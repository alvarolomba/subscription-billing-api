package com.subscription.billing.billing.dto;

import com.subscription.billing.billing.BillingInterval;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record PlanRequest(
        @NotBlank @Size(max = 120) String name,
        @NotNull @Min(0) Integer priceCents,
        @NotBlank @Pattern(regexp = "^[A-Za-z]{3}$") String currency,
        @NotNull BillingInterval billingInterval,
        @NotNull @Min(0) @Max(365) Integer trialDays
) {
}
