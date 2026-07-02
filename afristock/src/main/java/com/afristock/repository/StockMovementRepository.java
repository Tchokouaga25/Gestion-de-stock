package com.afristock.repository;

import com.afristock.model.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    List<StockMovement> findByTenantIdOrderByCreatedAtDesc(Long tenantId);
}
