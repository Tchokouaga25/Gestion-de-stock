package com.afristock.controller;

import com.afristock.service.InventoryService;
import com.afristock.service.SiteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;
    private final SiteService siteService;

    @GetMapping
    @PreAuthorize("hasAuthority('STOCK_READ')")
    public String list(Model model) {
        model.addAttribute("sessions", inventoryService.getAll());
        return "inventory/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAuthority('STOCK_WRITE')")
    public String chooseSite(Model model) {
        model.addAttribute("sites", siteService.getAll());
        return "inventory/choose-site";
    }

    @GetMapping("/count")
    @PreAuthorize("hasAuthority('STOCK_WRITE')")
    public String count(@RequestParam Long siteId, Model model) {
        model.addAttribute("site", siteService.getById(siteId));
        model.addAttribute("levels", inventoryService.stockOfSite(siteId));
        return "inventory/count";
    }

    @PostMapping("/save")
    @PreAuthorize("hasAuthority('STOCK_WRITE')")
    public String save(@RequestParam Long siteId,
                       @RequestParam(required = false) String note,
                       @RequestParam(name = "productId", required = false) List<Long> productIds,
                       @RequestParam(name = "counted", required = false) List<Integer> counted,
                       RedirectAttributes ra) {
        try {
            var session = inventoryService.validate(siteId, note, productIds, counted);
            ra.addFlashAttribute("success", "Inventaire enregistré et stock ajusté.");
            return "redirect:/inventory/" + session.getId();
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/inventory/count?siteId=" + siteId;
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('STOCK_READ')")
    public String detail(@PathVariable Long id, Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("session", inventoryService.getWithLines(id));
            return "inventory/detail";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/inventory";
        }
    }
}
