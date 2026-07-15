package com.afristock.controller;

import com.afristock.model.entity.Employee;
import com.afristock.model.entity.Leave;
import com.afristock.model.enums.ContractType;
import com.afristock.model.enums.LeaveStatus;
import com.afristock.model.enums.LeaveType;
import com.afristock.service.HrService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/hr")
@RequiredArgsConstructor
public class HrController {

    private final HrService hrService;

    // --- Employés ---

    @GetMapping("/employees")
    @PreAuthorize("hasAuthority('HR_READ')")
    public String employees(Model model) {
        model.addAttribute("kpis", hrService.getKpis());
        model.addAttribute("employeeRows", hrService.getEmployeesWithStats());
        return "hr/employees";
    }

    @GetMapping("/employees/add")
    @PreAuthorize("hasAuthority('HR_WRITE')")
    public String addForm(Model model) {
        model.addAttribute("employee", new Employee());
        model.addAttribute("contractTypes", ContractType.values());
        return "hr/employee-form";
    }

    @GetMapping("/employees/edit/{id}")
    @PreAuthorize("hasAuthority('HR_WRITE')")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("employee", hrService.getEmployee(id));
            model.addAttribute("contractTypes", ContractType.values());
            return "hr/employee-form";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/hr/employees";
        }
    }

    @PostMapping("/employees/save")
    @PreAuthorize("hasAuthority('HR_WRITE')")
    public String saveEmployee(@ModelAttribute Employee employee, RedirectAttributes ra) {
        try {
            hrService.saveEmployee(employee);
            ra.addFlashAttribute("success", "Employé enregistré.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/hr/employees";
    }

    @PostMapping("/employees/delete/{id}")
    @PreAuthorize("hasAuthority('HR_WRITE')")
    public String deleteEmployee(@PathVariable Long id, RedirectAttributes ra) {
        try {
            hrService.deleteEmployee(id);
            ra.addFlashAttribute("success", "Employé supprimé.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/hr/employees";
    }

    // --- Congés ---

    @GetMapping("/leaves")
    @PreAuthorize("hasAuthority('HR_READ')")
    public String leaves(Model model) {
        model.addAttribute("leaves", hrService.getLeaves());
        model.addAttribute("employees", hrService.getEmployees());
        model.addAttribute("types", LeaveType.values());
        return "hr/leaves";
    }

    @PostMapping("/leaves")
    @PreAuthorize("hasAuthority('HR_WRITE')")
    public String requestLeave(@RequestParam Long employeeId, @ModelAttribute Leave leave, RedirectAttributes ra) {
        try {
            hrService.requestLeave(leave, employeeId);
            ra.addFlashAttribute("success", "Demande enregistrée.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/hr/leaves";
    }

    @PostMapping("/leaves/{id}/status")
    @PreAuthorize("hasAuthority('HR_WRITE')")
    public String setLeaveStatus(@PathVariable Long id, @RequestParam LeaveStatus status, RedirectAttributes ra) {
        try {
            hrService.setLeaveStatus(id, status);
            ra.addFlashAttribute("success", "Statut mis à jour.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/hr/leaves";
    }

    // --- Pointage ---

    @GetMapping("/attendance")
    @PreAuthorize("hasAuthority('HR_READ')")
    public String attendance(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                             Model model) {
        LocalDate day = date != null ? date : LocalDate.now();
        model.addAttribute("date", day);
        model.addAttribute("employees", hrService.getEmployees());
        model.addAttribute("attendances", hrService.getAttendanceForDate(day));
        return "hr/attendance";
    }

    @PostMapping("/attendance")
    @PreAuthorize("hasAuthority('HR_WRITE')")
    public String mark(@RequestParam Long employeeId,
                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                       @RequestParam(defaultValue = "false") boolean present,
                       @RequestParam(required = false) String note,
                       RedirectAttributes ra) {
        try {
            hrService.mark(employeeId, date, present, note);
            ra.addFlashAttribute("success", "Pointage enregistré.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/hr/attendance?date=" + date;
    }
}
