package com.afristock.controller;

import com.afristock.model.entity.Company;
import com.afristock.model.entity.Feature;
import com.afristock.model.entity.Subscription;
import com.afristock.model.entity.SubscriptionPlan;
import com.afristock.repository.EmployeeRepository;
import com.afristock.repository.SaleRepository;
import com.afristock.repository.SiteRepository;
import com.afristock.repository.StockLevelRepository;
import com.afristock.service.CompanyAdminService;
import com.afristock.service.FeatureService;
import com.afristock.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
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
    private final SiteRepository siteRepository;
    private final EmployeeRepository employeeRepository;
    private final SaleRepository saleRepository;
    private final StockLevelRepository stockLevelRepository;

    @GetMapping
    public String home() {
        return "redirect:/super-admin/companies";
    }

    // --- Gestion des entreprises ---

    @GetMapping("/companies")
    public String companies(Model model) {
        var companies = companyAdminService.getAllCompanies();
        Map<Long, Subscription> subs = new HashMap<>();
        Map<Long, Long> siteCounts = new HashMap<>();
        Map<Long, Long> staffCounts = new HashMap<>();
        for (Company c : companies) {
            subscriptionService.forCompany(c.getId()).ifPresent(s -> subs.put(c.getId(), s));
            siteCounts.put(c.getId(), siteRepository.countByTenantId(c.getId()));
            staffCounts.put(c.getId(), employeeRepository.countByTenantId(c.getId()));
        }
        model.addAttribute("companies", companies);
        model.addAttribute("subscriptions", subs);
        model.addAttribute("siteCounts", siteCounts);
        model.addAttribute("staffCounts", staffCounts);
        model.addAttribute("plans", subscriptionService.getPlans());
        return "super-admin/companies";
    }

    /** Fiche entreprise en lecture seule (drill-down). Toutes les requêtes passent un tenantId
     *  explicite : le filtre Hibernate multi-tenant est de toute façon inactif pour un
     *  Super-Administrateur (TenantContext.getCurrentTenant() == null). */
    @GetMapping("/companies/{id}")
    public String companyDetail(@PathVariable Long id, Model model) {
        Company company = companyAdminService.getAllCompanies().stream()
                .filter(c -> c.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Entreprise introuvable."));

        LocalDateTime startOfMonth = LocalDateTime.now().withDayOfMonth(1).toLocalDate().atStartOfDay();
        LocalDateTime now = LocalDateTime.now();

        model.addAttribute("company", company);
        model.addAttribute("subscription", subscriptionService.forCompany(id).orElse(null));
        model.addAttribute("revenueThisMonth", saleRepository.revenueForPeriod(id, startOfMonth, now));
        model.addAttribute("salesThisMonth", saleRepository.findForPeriod(id, startOfMonth, now).size());
        model.addAttribute("sites", siteRepository.findByTenantIdOrderByName(id));
        model.addAttribute("staffCount", employeeRepository.countByTenantId(id));
        model.addAttribute("lowStock", stockLevelRepository.findLowStock(id));
        List<com.afristock.model.entity.Sale> recentSales = saleRepository.findByTenantIdOrderBySaleDateDesc(id);
        model.addAttribute("recentSales", recentSales.size() > 5 ? recentSales.subList(0, 5) : recentSales);
        return "super-admin/company-detail";
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
        model.addAttribute("newFeature", new Feature());
        return "super-admin/features";
    }

    @PostMapping("/features/{id}/toggle")
    public String toggleFeature(@PathVariable Long id, @RequestParam boolean enabled) {
        featureService.setEnabled(id, enabled);
        return "redirect:/super-admin/features";
    }

    @PostMapping("/features")
    public String createFeature(@RequestParam String code, @RequestParam String name,
                                @RequestParam(required = false) String category,
                                @RequestParam(required = false) String description,
                                RedirectAttributes ra) {
        try {
            featureService.create(code, name, category, description);
            ra.addFlashAttribute("success", "Fonctionnalité créée.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/super-admin/features";
    }

    @PostMapping("/features/{id}/update")
    public String updateFeature(@PathVariable Long id, @RequestParam String name,
                                @RequestParam(required = false) String category,
                                @RequestParam(required = false) String description,
                                RedirectAttributes ra) {
        try {
            featureService.update(id, name, category, description);
            ra.addFlashAttribute("success", "Fonctionnalité modifiée.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/super-admin/features";
    }

    @PostMapping("/features/{id}/delete")
    public String deleteFeature(@PathVariable Long id, RedirectAttributes ra) {
        try {
            featureService.delete(id);
            ra.addFlashAttribute("success", "Fonctionnalité supprimée.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/super-admin/features";
    }
}
