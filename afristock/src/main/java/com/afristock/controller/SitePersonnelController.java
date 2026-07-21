package com.afristock.controller;

import com.afristock.model.entity.Employee;
import com.afristock.model.entity.Leave;
import com.afristock.model.entity.Site;
import com.afristock.model.entity.User;
import com.afristock.model.enums.ContractType;
import com.afristock.model.enums.LeaveStatus;
import com.afristock.model.enums.LeaveType;
import com.afristock.service.HrService;
import com.afristock.service.SiteService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

/**
 * Gestion du personnel d'un site précis, accessible depuis sa fiche ({@code sites/detail.html}).
 *
 * <p>Réutilise {@link HrService} avec ses variantes filtrées par site plutôt que de dupliquer la
 * logique des pages RH transversales ({@code /hr/*}). Lecture ouverte aux admins ({@code HR_READ})
 * et, en lecture seule, au collaborateur affecté à ce site ({@code User.site}) ; toute écriture
 * (créer/modifier/supprimer un employé, statuer un congé, pointer une présence) reste réservée à
 * {@code HR_WRITE}, y compris pour le responsable de site.</p>
 */
@Controller
@RequestMapping("/sites/{siteId}/personnel")
@RequiredArgsConstructor
public class SitePersonnelController {

    private final SiteService siteService;
    private final HrService hrService;

    /** Vrai si l'utilisateur voit le personnel de ce site : admin ({@code HR_READ}) ou
     *  collaborateur affecté à ce site précis ({@code User.site}). Lève sinon : l'écriture
     *  (créer/modifier/supprimer/statuer/pointer) reste gérée séparément par {@code HR_WRITE}
     *  via {@code @PreAuthorize}, comme sur les pages RH transversales. */
    private void checkReadAccess(Long siteId, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        boolean hrRead = hasAuthority(currentUser, "HR_READ");
        boolean ownsSite = currentUser.getSite() != null && currentUser.getSite().getId().equals(siteId);
        if (!hrRead && !ownsSite) {
            throw new IllegalArgumentException("Accès refusé au personnel de ce site.");
        }
    }

    private boolean hasAuthority(User user, String authority) {
        return user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals(authority));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SITE_READ')")
    public String view(@PathVariable Long siteId,
                        @RequestParam(defaultValue = "overview") String tab,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                        Model model, Authentication authentication, RedirectAttributes ra) {
        try {
            checkReadAccess(siteId, authentication);
            Site site = siteService.getById(siteId);
            model.addAttribute("site", site);
            model.addAttribute("tab", tab);
            model.addAttribute("overview", hrService.getSiteOverview(siteId));

            switch (tab) {
                case "employees" -> {
                    model.addAttribute("employeeRows", hrService.getEmployeesWithStats(siteId));
                    model.addAttribute("contractTypes", ContractType.values());
                }
                case "leaves" -> {
                    model.addAttribute("leaves", hrService.getLeaves(siteId));
                    model.addAttribute("employees", hrService.getEmployees(siteId));
                    model.addAttribute("types", LeaveType.values());
                }
                case "attendance" -> {
                    LocalDate day = date != null ? date : LocalDate.now();
                    model.addAttribute("date", day);
                    model.addAttribute("employees", hrService.getEmployees(siteId));
                    model.addAttribute("attendances", hrService.getAttendanceForDate(siteId, day));
                }
                default -> model.addAttribute("employees", hrService.getEmployees(siteId));
            }
            return "sites/personnel";
        } catch (IllegalArgumentException e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/sites/" + siteId;
        }
    }

    @GetMapping("/employees/new")
    @PreAuthorize("hasAuthority('HR_WRITE')")
    public String newEmployeeForm(@PathVariable Long siteId, Model model) {
        model.addAttribute("employee", new Employee());
        model.addAttribute("contractTypes", ContractType.values());
        model.addAttribute("lockedSiteId", siteId);
        model.addAttribute("formAction", "/sites/" + siteId + "/personnel/employees/save");
        model.addAttribute("cancelUrl", "/sites/" + siteId + "/personnel?tab=employees");
        return "hr/employee-form";
    }

    @GetMapping("/employees/edit/{employeeId}")
    @PreAuthorize("hasAuthority('HR_WRITE')")
    public String editEmployeeForm(@PathVariable Long siteId, @PathVariable Long employeeId, Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("employee", hrService.getEmployee(employeeId));
            model.addAttribute("contractTypes", ContractType.values());
            model.addAttribute("sites", siteService.getAll());
            model.addAttribute("formAction", "/sites/" + siteId + "/personnel/employees/save");
            model.addAttribute("cancelUrl", "/sites/" + siteId + "/personnel?tab=employees");
            return "hr/employee-form";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/sites/" + siteId + "/personnel?tab=employees";
        }
    }

    @PostMapping("/employees/save")
    @PreAuthorize("hasAuthority('HR_WRITE')")
    public String saveEmployee(@PathVariable Long siteId, @ModelAttribute Employee employee, RedirectAttributes ra) {
        try {
            hrService.saveEmployee(employee, siteId);
            ra.addFlashAttribute("success", "Employé enregistré.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/sites/" + siteId + "/personnel?tab=employees";
    }

    @PostMapping("/employees/delete/{employeeId}")
    @PreAuthorize("hasAuthority('HR_WRITE')")
    public String deleteEmployee(@PathVariable Long siteId, @PathVariable Long employeeId, RedirectAttributes ra) {
        try {
            hrService.deleteEmployee(employeeId);
            ra.addFlashAttribute("success", "Employé supprimé.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/sites/" + siteId + "/personnel?tab=employees";
    }

    @PostMapping("/leaves")
    @PreAuthorize("hasAuthority('HR_WRITE')")
    public String requestLeave(@PathVariable Long siteId, @RequestParam Long employeeId, @ModelAttribute Leave leave, RedirectAttributes ra) {
        try {
            hrService.requestLeave(leave, employeeId);
            ra.addFlashAttribute("success", "Demande enregistrée.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/sites/" + siteId + "/personnel?tab=leaves";
    }

    @PostMapping("/leaves/{leaveId}/status")
    @PreAuthorize("hasAuthority('HR_WRITE')")
    public String setLeaveStatus(@PathVariable Long siteId, @PathVariable Long leaveId, @RequestParam LeaveStatus status, RedirectAttributes ra) {
        try {
            hrService.setLeaveStatus(leaveId, status);
            ra.addFlashAttribute("success", "Statut mis à jour.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/sites/" + siteId + "/personnel?tab=leaves";
    }

    @PostMapping("/attendance")
    @PreAuthorize("hasAuthority('HR_WRITE')")
    public String mark(@PathVariable Long siteId,
                        @RequestParam Long employeeId,
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
        return "redirect:/sites/" + siteId + "/personnel?tab=attendance&date=" + date;
    }
}
