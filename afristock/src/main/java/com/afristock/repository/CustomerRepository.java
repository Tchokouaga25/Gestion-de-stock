package com.afristock.repository;

import com.afristock.model.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerRepository extends JpaRepository<Customer, Long> {
    List<Customer> findByTenantIdOrderByName(Long tenantId);
    List<Customer> findByNameContainingIgnoreCaseAndTenantId(String name, Long tenantId);
    boolean existsByNameAndTenantId(String name, Long tenantId);
}
