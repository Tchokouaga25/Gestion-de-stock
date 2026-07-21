package com.afristock.repository;

import com.afristock.model.entity.Leave;
import com.afristock.model.enums.LeaveStatus;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface LeaveRepository extends JpaRepository<Leave, Long> {

    @Query("SELECT l FROM Leave l JOIN FETCH l.employee " +
            "WHERE l.tenantId = :tenantId ORDER BY l.startDate DESC")
    List<Leave> findAllForTenant(Long tenantId);

    long countByTenantIdAndStatus(Long tenantId, LeaveStatus status);

    @Query("SELECT l FROM Leave l JOIN FETCH l.employee " +
            "WHERE l.tenantId = :tenantId AND l.status = :status ORDER BY l.startDate DESC")
    List<Leave> findByTenantIdAndStatusOrderByStartDateDesc(Long tenantId, LeaveStatus status, Pageable pageable);

    @Query("SELECT COUNT(l) FROM Leave l WHERE l.tenantId = :tenantId " +
            "AND l.status = com.afristock.model.enums.LeaveStatus.APPROUVE " +
            "AND :today BETWEEN l.startDate AND l.endDate")
    long countOnLeaveToday(Long tenantId, LocalDate today);

    @Query("SELECT l FROM Leave l JOIN FETCH l.employee e " +
            "WHERE l.tenantId = :tenantId AND e.site.id = :siteId ORDER BY l.startDate DESC")
    List<Leave> findBySiteAndTenant(Long siteId, Long tenantId);

    @Query("SELECT COUNT(l) FROM Leave l JOIN l.employee e WHERE l.tenantId = :tenantId AND e.site.id = :siteId " +
            "AND l.status = com.afristock.model.enums.LeaveStatus.APPROUVE " +
            "AND :today BETWEEN l.startDate AND l.endDate")
    long countOnLeaveTodayForSite(Long siteId, Long tenantId, LocalDate today);
}
