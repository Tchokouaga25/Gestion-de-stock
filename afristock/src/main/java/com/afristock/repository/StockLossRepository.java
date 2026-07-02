package com.afristock.repository;

import com.afristock.model.entity.StockLoss;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface StockLossRepository extends JpaRepository<StockLoss, Long> {

    @Query("SELECT l FROM StockLoss l JOIN FETCH l.product JOIN FETCH l.site " +
            "WHERE l.tenantId = :tenantId ORDER BY l.declaredAt DESC")
    List<StockLoss> findAllForTenant(Long tenantId);
}
