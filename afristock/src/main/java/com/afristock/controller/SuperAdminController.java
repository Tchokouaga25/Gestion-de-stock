package com.afristock.controller;

import com.afristock.model.entity.Company;
import com.afristock.model.entity.Subscription;
import com.afristock.model.entity.SubscriptionPlan;
import com.afristock.service.CompanyAdminService;
import com.afristock.service.FeatureService;
import com.afristock.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.Map;

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
    private final SubscriptionService subscriptionService;

    @GetMapping
    public String home() {
        return "redirect:/super-admin/companies";
    }

    // --- Gestion des entreprises ---

    @GetMapping("/companies")
    public String companies(Model model) {
        var companies = companyAdminService.getAllCompanies();
        Map<Long, Subscription> subs = new HashMap<>();
        for (Company c : companies) {
            subscriptionService.forCompany(c.getId()).ifPresent(s -> subs.put(c.getId(), s));
        }
        model.addAttribute("companies", companies);
        model.addAttribute("subscriptions", subs);
        model.addAttribute("plans", subscriptionService.getPlans());
        return "super-admin/companies";
    }

    // --- Offres d'abonnement ---

    @GetMapping("/plans")
    public String plans(Model model) {
        model.addAttribute("plans", subscriptionService.getPlans());
        model.addAttribute("newPlan", new SubscriptionPlan());
        return "super-admin/plans";
    }

    @PostMapping("/plans")
    public String savePlan(@ModelAttribute("newPlan") SubscriptionPlan plan, RedirectAttributes ra) {
        try {
            subscriptionService.savePlan(plan);
            ra.addFlashAttribute("success", "Offre enregistrée.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/super-admin/plans";
    }

    @PostMapping("/companies/{id}/subscribe")
    public String subscribe(@PathVariable Long id, @RequestParam Long planId,
                            @RequestParam(defaultValue = "1") int months, RedirectAttributes ra) {
        try {
            subscriptionService.subscribe(id, planId, months);
            ra.addFlashAttribute("success", "Abonnement mis à jour.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/super-admin/companies";
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
