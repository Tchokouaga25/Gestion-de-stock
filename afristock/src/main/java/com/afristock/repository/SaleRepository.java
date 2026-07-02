package com.afristock.repository;

import com.afristock.model.entity.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SaleRepository extends JpaRepository<Sale, Long> {

    List<Sale> findByTenantIdOrderBySaleDateDesc(Long tenantId);

    long countByTenantId(Long tenantId);

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
}
