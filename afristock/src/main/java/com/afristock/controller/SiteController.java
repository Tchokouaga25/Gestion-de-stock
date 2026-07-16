package com.afristock.controller;

import com.afristock.model.entity.Site;
import com.afristock.model.enums.SiteType;
import com.afristock.service.SiteService;
import com.afristock.service.StockLevelService;
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
