package com.afristock.repository;

import com.afristock.model.entity.StockMovement;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import java.util.List;

public interface StockMovementRepository extends JpaRepository<StockMovement, Long> {
    List<StockMovement> findByTenantIdOrderByCreatedAtDesc(Long tenantId);

    /** Mouvements de stock récents d'un site donné (espace boutique). */
    @Query("SELECT m FROM StockMovement m JOIN FETCH m.product " +
            "WHERE m.tenantId = :tenantId AND m.site.id = :siteId ORDER BY m.createdAt DESC")
    List<StockMovement> findBySiteIdAndTenantIdOrderByCreatedAtDesc(Long siteId, Long tenantId);
}
