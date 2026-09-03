package com.afristock.repository;

import com.afristock.model.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByTenantId(Long tenantId);
    boolean existsByNameAndTenantId(String name, Long tenantId);
}
