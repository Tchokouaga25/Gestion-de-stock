package com.afristock.repository;

import com.afristock.model.entity.InventorySession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface InventorySessionRepository extends JpaRepository<InventorySession, Long> {

    @Query("SELECT s FROM InventorySession s JOIN FETCH s.site " +
            "WHERE s.tenantId = :tenantId ORDER BY s.countedAt DESC")
    List<InventorySession> findAllForTenant(Long tenantId);

    @Query("SELECT s FROM InventorySession s JOIN FETCH s.site LEFT JOIN FETCH s.lines l LEFT JOIN FETCH l.product " +
            "WHERE s.id = :id AND s.tenantId = :tenantId")
    Optional<InventorySession> findByIdWithLines(Long id, Long tenantId);
}
