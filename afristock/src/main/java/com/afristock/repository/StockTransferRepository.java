package com.afristock.repository;

import com.afristock.model.entity.StockTransfer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StockTransferRepository extends JpaRepository<StockTransfer, Long> {

    @Query("SELECT t FROM StockTransfer t JOIN FETCH t.product JOIN FETCH t.fromSite JOIN FETCH t.toSite " +
            "WHERE t.tenantId = :tenantId ORDER BY t.transferredAt DESC")
    List<StockTransfer> findAllForTenant(Long tenantId);
}
