package com.subscription.billing.billing;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    Optional<Customer> findByIdAndOwnerId(Long id, Long ownerId);

    boolean existsByOwnerIdAndEmail(Long ownerId, String email);
}
