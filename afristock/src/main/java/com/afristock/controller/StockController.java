package com.afristock.controller;

import com.afristock.service.ProductService;
import com.afristock.service.SiteService;
import com.afristock.service.StockLevelService;
import com.afristock.service.StockTransferService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

/**
 * Stock multi-sites (Phase 3) : niveaux de stock par site et transferts entre sites.
 */
@Controller
@RequestMapping("/stock")
@RequiredArgsConstructor
public class StockController {

    private final StockLevelService stockLevelService;
    private final StockTransferService stockTransferService;
    private final ProductService productService;
    private final SiteService siteService;

    @GetMapping
    @PreAuthorize("hasAuthority('STOCK_READ')")
    public String levels(Model model) {
        model.addAttribute("levels", stockLevelService.getAll());
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("sites", siteService.getAll());
        return "stock/levels";
    }

    @PostMapping("/adjust")
    @PreAuthorize("hasAuthority('STOCK_WRITE')")
    public String adjust(@RequestParam Long productId,
                         @RequestParam Long siteId,
                         @RequestParam Integer quantity,
                         RedirectAttributes ra) {
        try {
            stockLevelService.setQuantity(productId, siteId, quantity);
            ra.addFlashAttribute("success", "Stock mis à jour.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/stock";
    }

    @GetMapping("/transfer")
    @PreAuthorize("hasAuthority('STOCK_WRITE')")
    public String transferForm(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("sites", siteService.getAll());
        return "stock/transfer";
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasAuthority('STOCK_WRITE')")
    public String transfer(@RequestParam Long productId,
                           @RequestParam Long fromSiteId,
                           @RequestParam Long toSiteId,
                           @RequestParam Integer quantity,
                           @RequestParam(required = false) String reason,
                           RedirectAttributes ra) {
        try {
            stockTransferService.transfer(productId, fromSiteId, toSiteId, quantity, reason);
            ra.addFlashAttribute("success", "Transfert effectué avec succès.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/stock/transfers";
    }

    @GetMapping("/transfers")
    @PreAuthorize("hasAuthority('STOCK_READ')")
    public String transfers(Model model) {
        model.addAttribute("transfers", stockTransferService.getHistory());
        return "stock/transfers";
    }
}
