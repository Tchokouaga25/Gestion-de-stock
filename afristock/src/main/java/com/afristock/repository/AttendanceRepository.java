package com.afristock.repository;

import com.afristock.model.entity.Attendance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {

    Optional<Attendance> findByEmployeeIdAndWorkDateAndTenantId(Long employeeId, LocalDate workDate, Long tenantId);

    @Query("SELECT a FROM Attendance a JOIN FETCH a.employee " +
            "WHERE a.tenantId = :tenantId AND a.workDate = :date ORDER BY a.employee.lastName")
    List<Attendance> findForDate(Long tenantId, LocalDate date);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.tenantId = :tenantId AND a.workDate BETWEEN :from AND :to")
    long countForPeriod(Long tenantId, LocalDate from, LocalDate to);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.tenantId = :tenantId AND a.workDate BETWEEN :from AND :to AND a.present = true")
    long countPresentForPeriod(Long tenantId, LocalDate from, LocalDate to);

    @Query("SELECT a.employee.id, COUNT(a), SUM(CASE WHEN a.present = true THEN 1 ELSE 0 END) " +
            "FROM Attendance a WHERE a.tenantId = :tenantId GROUP BY a.employee.id")
    List<Object[]> attendanceStatsByEmployee(Long tenantId);

    @Query("SELECT a FROM Attendance a JOIN FETCH a.employee e " +
            "WHERE a.tenantId = :tenantId AND e.site.id = :siteId AND a.workDate = :date ORDER BY a.employee.lastName")
    List<Attendance> findForSiteAndDate(Long siteId, Long tenantId, LocalDate date);

    @Query("SELECT COUNT(a) FROM Attendance a JOIN a.employee e " +
            "WHERE a.tenantId = :tenantId AND e.site.id = :siteId AND a.workDate BETWEEN :from AND :to")
    long countForPeriodAndSite(Long siteId, Long tenantId, LocalDate from, LocalDate to);

    @Query("SELECT COUNT(a) FROM Attendance a JOIN a.employee e " +
            "WHERE a.tenantId = :tenantId AND e.site.id = :siteId AND a.workDate BETWEEN :from AND :to AND a.present = true")
    long countPresentForPeriodAndSite(Long siteId, Long tenantId, LocalDate from, LocalDate to);

    @Query("SELECT a.employee.id, COUNT(a), SUM(CASE WHEN a.present = true THEN 1 ELSE 0 END) " +
            "FROM Attendance a JOIN a.employee e WHERE a.tenantId = :tenantId AND e.site.id = :siteId GROUP BY a.employee.id")
    List<Object[]> attendanceStatsBySiteAndEmployee(Long siteId, Long tenantId);
}
