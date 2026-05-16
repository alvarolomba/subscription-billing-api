package com.subscription.billing.billing;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    @EntityGraph(attributePaths = {"customer", "subscription"})
    List<Invoice> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    @EntityGraph(attributePaths = {"customer", "subscription"})
    Optional<Invoice> findByIdAndOwnerId(Long id, Long ownerId);

    long countByOwnerIdAndStatus(Long ownerId, InvoiceStatus status);

    @Query("select coalesce(sum(invoice.amountCents), 0) from Invoice invoice where invoice.owner.id = :ownerId and invoice.status = :status")
    long sumAmountByOwnerIdAndStatus(@Param("ownerId") Long ownerId, @Param("status") InvoiceStatus status);
}
