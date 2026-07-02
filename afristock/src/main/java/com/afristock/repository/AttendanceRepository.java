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
}
