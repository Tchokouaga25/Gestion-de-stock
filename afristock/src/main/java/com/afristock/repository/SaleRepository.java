package com.afristock.repository;

import com.afristock.model.entity.Sale;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    List<Sale> findByTenantIdOrderBySaleDateDesc(Long tenantId);

    Page<Sale> findByTenantIdOrderBySaleDateDesc(Long tenantId, Pageable pageable);

    long countByTenantId(Long tenantId);

    @Query("SELECT COUNT(s) FROM Sale s WHERE s.tenantId = :tenantId AND s.totalAmount > s.amountPaid")
    long countUnpaidByTenantId(Long tenantId);

    @Query("SELECT s FROM Sale s WHERE s.tenantId = :tenantId AND s.totalAmount > s.amountPaid " +
            "ORDER BY s.saleDate DESC")
    List<Sale> findUnpaidByTenantId(Long tenantId, Pageable pageable);

    @Query("SELECT s FROM Sale s LEFT JOIN FETCH s.items i LEFT JOIN FETCH i.product " +
            "WHERE s.id = :id AND s.tenantId = :tenantId")
    Optional<Sale> findByIdWithItems(Long id, Long tenantId);

    @Query("SELECT s FROM Sale s WHERE s.tenantId = :tenantId " +
            "AND s.saleDate >= :from AND s.saleDate < :to ORDER BY s.saleDate DESC")
    List<Sale> findForPeriod(Long tenantId, LocalDateTime from, LocalDateTime to);

    @Query("SELECT COALESCE(SUM(s.totalAmount), 0) FROM Sale s WHERE s.tenantId = :tenantId " +
            "AND s.saleDate >= :from AND s.saleDate < :to")
    double revenueForPeriod(Long tenantId, LocalDateTime from, LocalDateTime to);

    @Query("SELECT COALESCE(SUM(i.quantity), 0) FROM SaleItem i " +
            "WHERE i.sale.tenantId = :tenantId AND i.sale.saleDate >= :from AND i.sale.saleDate < :to")
    long itemsSoldForPeriod(Long tenantId, LocalDateTime from, LocalDateTime to);

    @Query("SELECT COALESCE(SUM((i.unitPrice - COALESCE(i.product.purchasePrice, 0)) * i.quantity), 0) FROM SaleItem i " +
            "WHERE i.sale.tenantId = :tenantId AND i.sale.saleDate >= :from AND i.sale.saleDate < :to")
    double grossProfitForPeriod(Long tenantId, LocalDateTime from, LocalDateTime to);

    /** Chiffre d'affaires par site sur la période (0..N lignes : [siteId, revenue]). */
    @Query("SELECT s.site.id, COALESCE(SUM(s.totalAmount), 0) FROM Sale s " +
            "WHERE s.tenantId = :tenantId AND s.saleDate >= :from AND s.saleDate < :to GROUP BY s.site.id")
    List<Object[]> revenueBySiteForPeriod(Long tenantId, LocalDateTime from, LocalDateTime to);

    /** Ventes d'un site sur une période donnée (espace boutique), les plus récentes en premier. */
    @Query("SELECT s FROM Sale s WHERE s.tenantId = :tenantId AND s.site.id = :siteId " +
            "AND s.saleDate >= :from AND s.saleDate < :to ORDER BY s.saleDate DESC")
    List<Sale> findForSiteAndPeriod(Long tenantId, Long siteId, LocalDateTime from, LocalDateTime to);
}
