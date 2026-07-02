package com.afristock.service;

import com.afristock.model.entity.Attendance;
import com.afristock.model.entity.Employee;
import com.afristock.model.entity.Leave;
import com.afristock.model.enums.LeaveStatus;
import com.afristock.repository.AttendanceRepository;
import com.afristock.repository.EmployeeRepository;
import com.afristock.repository.LeaveRepository;
import com.afristock.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Ressources humaines (Phase 7) : employés, congés/absences, pointage.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class HrService {

    private final EmployeeRepository employeeRepository;
    private final LeaveRepository leaveRepository;
    private final AttendanceRepository attendanceRepository;

    // --- Employés ---

    @Transactional(readOnly = true)
    public List<Employee> getEmployees() {
        return employeeRepository.findByTenantIdOrderByLastNameAscFirstNameAsc(TenantContext.getCurrentTenant());
    }

    @Transactional(readOnly = true)
    public Employee getEmployee(Long id) {
        return ownedEmployee(id);
    }

    public Employee saveEmployee(Employee employee) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (employee.getId() == null) {
            employee.setTenantId(tenantId);
            return employeeRepository.save(employee);
        }
        Employee existing = ownedEmployee(employee.getId());
        existing.setFirstName(employee.getFirstName());
        existing.setLastName(employee.getLastName());
        existing.setPosition(employee.getPosition());
        existing.setContractType(employee.getContractType());
        existing.setHireDate(employee.getHireDate());
        existing.setBaseSalary(employee.getBaseSalary());
        existing.setPhone(employee.getPhone());
        existing.setEmail(employee.getEmail());
        existing.setActive(employee.isActive());
        return employeeRepository.save(existing);
    }

    public void deleteEmployee(Long id) {
        employeeRepository.delete(ownedEmployee(id));
    }

    // --- Congés / absences ---

    @Transactional(readOnly = true)
    public List<Leave> getLeaves() {
        return leaveRepository.findAllForTenant(TenantContext.getCurrentTenant());
    }

    public void requestLeave(Leave leave, Long employeeId) {
        Employee employee = ownedEmployee(employeeId);
        if (leave.getStartDate() == null || leave.getEndDate() == null
                || leave.getEndDate().isBefore(leave.getStartDate())) {
            throw new IllegalArgumentException("Dates de congé invalides.");
        }
        leave.setEmployee(employee);
        leave.setStatus(LeaveStatus.EN_ATTENTE);
        leave.setTenantId(TenantContext.getCurrentTenant());
        leaveRepository.save(leave);
    }

    public void setLeaveStatus(Long leaveId, LeaveStatus status) {
        Leave leave = leaveRepository.findById(leaveId)
                .orElseThrow(() -> new IllegalArgumentException("Congé introuvable."));
        if (!leave.getTenantId().equals(TenantContext.getCurrentTenant())) {
            throw new IllegalArgumentException("Congé introuvable.");
        }
        leave.setStatus(status);
        leaveRepository.save(leave);
    }

    // --- Pointage ---

    @Transactional(readOnly = true)
    public List<Attendance> getAttendanceForDate(LocalDate date) {
        return attendanceRepository.findForDate(TenantContext.getCurrentTenant(), date);
    }

    public void mark(Long employeeId, LocalDate date, boolean present, String note) {
        Long tenantId = TenantContext.getCurrentTenant();
        Employee employee = ownedEmployee(employeeId);
        Attendance attendance = attendanceRepository
                .findByEmployeeIdAndWorkDateAndTenantId(employeeId, date, tenantId)
                .orElseGet(() -> {
                    Attendance a = new Attendance();
                    a.setEmployee(employee);
                    a.setWorkDate(date);
                    a.setTenantId(tenantId);
                    return a;
                });
        attendance.setPresent(present);
        attendance.setNote(note);
        attendanceRepository.save(attendance);
    }

    private Employee ownedEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employé introuvable."));
        if (!employee.getTenantId().equals(TenantContext.getCurrentTenant())) {
            throw new IllegalArgumentException("Employé introuvable.");
        }
        return employee;
    }
}
