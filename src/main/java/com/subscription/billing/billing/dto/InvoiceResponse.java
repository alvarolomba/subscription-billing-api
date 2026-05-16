package com.subscription.billing.billing.dto;

import com.subscription.billing.billing.Invoice;
import com.subscription.billing.billing.InvoiceStatus;
import java.time.Instant;
import java.time.LocalDate;

public record InvoiceResponse(
        Long id,
        Long customerId,
        String customerEmail,
        Long subscriptionId,
        Integer amountCents,
        String currency,
        InvoiceStatus status,
        LocalDate dueDate,
        Instant paidAt,
        Instant createdAt
) {
    public static InvoiceResponse from(Invoice invoice) {
        return new InvoiceResponse(
                invoice.getId(),
                invoice.getCustomer().getId(),
                invoice.getCustomer().getEmail(),
                invoice.getSubscription().getId(),
                invoice.getAmountCents(),
                invoice.getCurrency(),
                invoice.getStatus(),
                invoice.getDueDate(),
                invoice.getPaidAt(),
                invoice.getCreatedAt()
        );
    }
}
