package com.afristock.controller;

import com.afristock.service.CompanyAdminService;
import com.afristock.service.FeatureService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Espace dédié au Super-Administrateur de la plateforme SaaS.
 *
 * <p>Protégé deux fois : par l'URL ({@code /super-admin/**} dans SecurityConfig) et par
 * {@code @PreAuthorize} au niveau de la classe.</p>
 */
@Controller
@RequestMapping("/super-admin")
@PreAuthorize("hasRole('SUPER_ADMIN')")
@RequiredArgsConstructor
public class SuperAdminController {

    private final CompanyAdminService companyAdminService;
    private final FeatureService featureService;

    @GetMapping
    public String home() {
        return "redirect:/super-admin/companies";
    }

    // --- Gestion des entreprises ---

    @GetMapping("/companies")
    public String companies(Model model) {
        model.addAttribute("companies", companyAdminService.getAllCompanies());
        return "super-admin/companies";
    }

    @PostMapping("/companies/{id}/suspend")
    public String suspend(@PathVariable Long id) {
        companyAdminService.suspend(id);
        return "redirect:/super-admin/companies";
    }

    @PostMapping("/companies/{id}/activate")
    public String activate(@PathVariable Long id) {
        companyAdminService.activate(id);
        return "redirect:/super-admin/companies";
    }

    // --- Fonctionnalités (feature flags) ---

    @GetMapping("/features")
    public String features(Model model) {
        model.addAttribute("features", featureService.getAll());
        return "super-admin/features";
    }

    @PostMapping("/features/{id}/toggle")
    public String toggleFeature(@PathVariable Long id,
                                @org.springframework.web.bind.annotation.RequestParam boolean enabled) {
        featureService.setEnabled(id, enabled);
        return "redirect:/super-admin/features";
    }
}
