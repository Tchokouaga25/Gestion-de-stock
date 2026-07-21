package com.afristock.repository;

import com.afristock.model.entity.PurchaseOrder;
import com.afristock.model.enums.PurchaseStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, Long> {

    @Query("SELECT p FROM PurchaseOrder p JOIN FETCH p.supplier JOIN FETCH p.site " +
            "WHERE p.tenantId = :tenantId ORDER BY p.orderDate DESC")
    List<PurchaseOrder> findAllForTenant(Long tenantId);

    long countByTenantId(Long tenantId);

    long countByTenantIdAndStatus(Long tenantId, PurchaseStatus status);

    @Query("SELECT p FROM PurchaseOrder p JOIN FETCH p.supplier JOIN FETCH p.site " +
            "WHERE p.tenantId = :tenantId AND p.status = :status ORDER BY p.orderDate DESC")
    List<PurchaseOrder> findByTenantIdAndStatusOrderByOrderDateDesc(Long tenantId, PurchaseStatus status, Pageable pageable);

    @Query("SELECT p FROM PurchaseOrder p JOIN FETCH p.supplier JOIN FETCH p.site " +
            "LEFT JOIN FETCH p.items i LEFT JOIN FETCH i.product " +
            "WHERE p.id = :id AND p.tenantId = :tenantId")
    Optional<PurchaseOrder> findByIdWithItems(Long id, Long tenantId);

    @Query("SELECT COALESCE(SUM(p.totalAmount - p.amountPaid), 0) FROM PurchaseOrder p " +
            "WHERE p.supplier.id = :supplierId AND p.tenantId = :tenantId AND p.status <> com.afristock.model.enums.PurchaseStatus.ANNULEE")
    double totalDebtForSupplier(Long supplierId, Long tenantId);
}
