package com.afristock.repository;

import com.afristock.model.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    List<Employee> findByTenantIdOrderByLastNameAscFirstNameAsc(Long tenantId);
    long countByTenantId(Long tenantId);
    long countBySiteIdAndTenantId(Long siteId, Long tenantId);
}
