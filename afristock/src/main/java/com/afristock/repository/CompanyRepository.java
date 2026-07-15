package com.afristock.repository;

import com.afristock.model.entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CompanyRepository extends JpaRepository<Company, Long> {
    boolean existsByEmail(String email);

    @Query("SELECT c FROM Company c WHERE :q IS NULL OR :q = '' " +
            "OR LOWER(c.name) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(c.email) LIKE LOWER(CONCAT('%', :q, '%')) " +
            "OR LOWER(c.city) LIKE LOWER(CONCAT('%', :q, '%'))")
    Page<Company> search(@Param("q") String q, Pageable pageable);
}
