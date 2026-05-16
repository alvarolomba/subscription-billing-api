package com.subscription.billing.billing;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlanRepository extends JpaRepository<Plan, Long> {
    List<Plan> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    Optional<Plan> findByIdAndOwnerId(Long id, Long ownerId);
}
