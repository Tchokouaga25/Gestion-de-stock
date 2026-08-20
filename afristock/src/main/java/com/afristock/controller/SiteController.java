package com.afristock.controller;

import com.afristock.model.entity.Site;
import com.afristock.model.enums.SiteType;
import com.afristock.repository.SiteRepository;
import com.afristock.security.TenantContext;
import com.afristock.service.SiteService;
import com.afristock.service.StockLevelService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Gestion des sites (boutiques / entrepôts).
 *
 * <p>Lecture autorisée à tout employé ({@code SITE_READ}) ; création/modification/suppression
 * réservées aux profils disposant des permissions correspondantes (l'administrateur PME).</p>
 */
@Controller
@RequestMapping("/sites")
@RequiredArgsConstructor
public class SiteController {

    private final SiteService siteService;
    private final StockLevelService stockLevelService;
    private final SiteRepository siteRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('SITE_READ')")
    public String list(Model model) {
        model.addAttribute("siteStats", siteService.getShopCardStats());
        model.addAttribute("totalStockValue", stockLevelService.getTotalStockValue());
        model.addAttribute("totalAlerts", stockLevelService.getLowStock().size());
        return "sites/list";
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SITE_READ')")
    public String detail(@PathVariable Long id, Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("shop", siteService.getShopDetail(id));
            return "sites/detail";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/sites";
        }
    }

    @GetMapping("/add")
    @PreAuthorize("hasAuthority('SITE_WRITE')")
    public String showAddForm(Model model) {
        model.addAttribute("site", new Site());
        model.addAttribute("types", SiteType.values());
        return "sites/form";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('SITE_WRITE')")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("site", siteService.getById(id));
            model.addAttribute("types", SiteType.values());
            return "sites/form";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/sites";
        }
    }

    @PostMapping("/save")
    @PreAuthorize("hasAuthority('SITE_WRITE')")
    public String save(@ModelAttribute Site site, RedirectAttributes ra) {
        try {
            siteService.save(site);
            ra.addFlashAttribute("success", "Site enregistré avec succès.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/sites";
    }

    /**
     * Change le "site actif" affiché dans la sidebar (sélecteur), stocké en session. N'importe quel
     * collaborateur de l'entreprise peut basculer (c'est son contexte de travail personnel, pas une
     * action de gestion) — pas de {@code @PreAuthorize} au-delà de l'authentification déjà requise
     * globalement. Le site choisi est vérifié comme appartenant au tenant courant avant d'être retenu.
     */
    @PostMapping("/switch")
    public String switchSite(@RequestParam Long siteId, HttpSession session, HttpServletRequest request) {
        Long tenantId = TenantContext.getCurrentTenant();
        siteRepository.findById(siteId)
                .filter(s -> s.getTenantId().equals(tenantId) && s.isActive())
                .ifPresent(s -> session.setAttribute(GlobalControllerAdvice.CURRENT_SITE_SESSION_KEY, s.getId()));
        // Revient sur la page d'où le changement a été déclenché (sélecteur visible partout dans la
        // sidebar), en ne faisant confiance qu'à un Referer du même hôte (sinon /dashboard par défaut).
        String referer = request.getHeader("Referer");
        if (referer != null && referer.startsWith(request.getRequestURL().toString().replace(request.getRequestURI(), "/"))) {
            return "redirect:" + referer;
        }
        return "redirect:/dashboard";
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('SITE_DELETE')")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            siteService.delete(id);
            ra.addFlashAttribute("success", "Site supprimé.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/sites";
    }
}
