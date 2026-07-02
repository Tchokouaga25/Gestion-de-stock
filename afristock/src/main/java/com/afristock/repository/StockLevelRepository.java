package com.afristock.repository;

import com.afristock.model.entity.StockLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StockLevelRepository extends JpaRepository<StockLevel, Long> {

    Optional<StockLevel> findByProductIdAndSiteIdAndTenantId(Long productId, Long siteId, Long tenantId);

    @Query("SELECT sl FROM StockLevel sl JOIN FETCH sl.product JOIN FETCH sl.site " +
            "WHERE sl.tenantId = :tenantId ORDER BY sl.site.name, sl.product.name")
    List<StockLevel> findAllForTenant(Long tenantId);

    @Query("SELECT COALESCE(SUM(sl.quantity), 0) FROM StockLevel sl " +
            "WHERE sl.product.id = :productId AND sl.tenantId = :tenantId")
    int sumQuantityForProduct(Long productId, Long tenantId);

    List<StockLevel> findBySiteIdAndTenantId(Long siteId, Long tenantId);

    /** Lignes de stock en rupture ou sous le seuil d'alerte du produit. */
    @Query("SELECT sl FROM StockLevel sl JOIN FETCH sl.product p JOIN FETCH sl.site " +
            "WHERE sl.tenantId = :tenantId AND p.minThreshold IS NOT NULL " +
            "AND sl.quantity <= p.minThreshold ORDER BY sl.site.name, p.name")
    List<StockLevel> findLowStock(Long tenantId);
}
