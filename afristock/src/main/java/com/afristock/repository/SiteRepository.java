package com.afristock.repository;

import com.afristock.model.entity.Site;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SiteRepository extends JpaRepository<Site, Long> {
    List<Site> findByTenantIdOrderByName(Long tenantId);
    boolean existsByNameAndTenantId(String name, Long tenantId);
    long countByTenantId(Long tenantId);
}
