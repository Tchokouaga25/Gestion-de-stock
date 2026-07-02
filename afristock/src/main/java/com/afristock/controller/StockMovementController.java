package com.afristock.controller;

import com.afristock.model.enums.MovementType;
import com.afristock.service.ProductService;
import com.afristock.service.SiteService;
import com.afristock.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/movements")
@RequiredArgsConstructor
public class StockMovementController {

    private final StockService stockService;
    private final ProductService productService;
    private final SiteService siteService;

    @GetMapping("/new")
    @PreAuthorize("hasAuthority('STOCK_WRITE')")
    public String showMovementForm(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("sites", siteService.getAll());
        model.addAttribute("types", MovementType.values());
        return "movements/form";
    }

    @PostMapping("/save")
    @PreAuthorize("hasAuthority('STOCK_WRITE')")
    public String saveMovement(@RequestParam Long productId,
                               @RequestParam Long siteId,
                               @RequestParam Integer quantity,
                               @RequestParam MovementType type,
                               @RequestParam(required = false) String reason,
                               RedirectAttributes ra) {
        try {
            stockService.recordMovement(productId, siteId, quantity, type, reason);
            ra.addFlashAttribute("success", "Mouvement enregistré avec succès.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/movements/new";
        }
        return "redirect:/stock";
    }
}
