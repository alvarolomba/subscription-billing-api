package com.subscription.billing.billing.dto;

public record BillingStatsResponse(
        long activeSubscriptions,
        long trialingSubscriptions,
        long openInvoices,
        long monthlyRecurringRevenueCents,
        long paidRevenueCents
) {
}
