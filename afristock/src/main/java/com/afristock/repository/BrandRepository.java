package com.afristock.repository;

import com.afristock.model.entity.Brand;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BrandRepository extends JpaRepository<Brand, Long> {
    List<Brand> findByTenantIdOrderByName(Long tenantId);
    boolean existsByNameAndTenantId(String name, Long tenantId);
}
