package com.afristock.controller;

import com.afristock.model.enums.LossType;
import com.afristock.service.ProductService;
import com.afristock.service.SiteService;
import com.afristock.service.StockLossService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/losses")
@RequiredArgsConstructor
public class StockLossController {

    private final StockLossService stockLossService;
    private final ProductService productService;
    private final SiteService siteService;

    @GetMapping
    @PreAuthorize("hasAuthority('STOCK_READ')")
    public String list(Model model) {
        model.addAttribute("losses", stockLossService.getAll());
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("sites", siteService.getAll());
        model.addAttribute("types", LossType.values());
        return "stock/losses";
    }

    @PostMapping
    @PreAuthorize("hasAuthority('STOCK_WRITE')")
    public String declare(@RequestParam Long productId,
                          @RequestParam Long siteId,
                          @RequestParam Integer quantity,
                          @RequestParam LossType type,
                          @RequestParam(required = false) String reason,
                          RedirectAttributes ra) {
        try {
            stockLossService.declareLoss(productId, siteId, quantity, type, reason);
            ra.addFlashAttribute("success", "Perte déclarée et stock ajusté.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/losses";
    }
}
