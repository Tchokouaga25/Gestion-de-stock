package com.afristock.repository;

import com.afristock.model.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByTenantId(Long tenantId);
    boolean existsByReferenceAndTenantId(String reference, Long tenantId);
    List<Product> findByNameContainingIgnoreCaseAndTenantId(String name, Long tenantId);
    List<Product> findByCategoryIdAndTenantId(Long categoryId, Long tenantId);
}
