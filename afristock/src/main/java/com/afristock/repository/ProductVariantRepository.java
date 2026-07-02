package com.afristock.repository;

import com.afristock.model.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductVariantRepository extends JpaRepository<ProductVariant, Long> {
    List<ProductVariant> findByProductIdAndTenantId(Long productId, Long tenantId);
}
