package com.afristock.controller;

import com.afristock.service.ProductService;
import com.afristock.service.PurchaseService;
import com.afristock.service.SiteService;
import com.afristock.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/purchases")
@RequiredArgsConstructor
public class PurchaseController {

    private final PurchaseService purchaseService;
    private final SupplierService supplierService;
    private final SiteService siteService;
    private final ProductService productService;

    @GetMapping
    @PreAuthorize("hasAuthority('PURCHASE_READ')")
    public String list(Model model) {
        model.addAttribute("orders", purchaseService.getAll());
        return "purchases/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAuthority('PURCHASE_WRITE')")
    public String newOrder(Model model) {
        model.addAttribute("suppliers", supplierService.getAll());
        model.addAttribute("sites", siteService.getAll());
        model.addAttribute("products", productService.getAllProducts());
        return "purchases/new";
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PURCHASE_WRITE')")
    public String create(@RequestParam Long supplierId,
                         @RequestParam Long siteId,
                         @RequestParam(name = "productId", required = false) List<Long> productIds,
                         @RequestParam(name = "quantity", required = false) List<Integer> quantities,
                         @RequestParam(name = "unitCost", required = false) List<Double> unitCosts,
                         RedirectAttributes ra) {
        try {
            var order = purchaseService.createOrder(supplierId, siteId, productIds, quantities, unitCosts);
            ra.addFlashAttribute("success", "Commande " + order.getReference() + " créée.");
            return "redirect:/purchases/" + order.getId();
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/purchases/new";
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PURCHASE_READ')")
    public String detail(@PathVariable Long id, Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("order", purchaseService.getWithItems(id));
            return "purchases/detail";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/purchases";
        }
    }

    @PostMapping("/{id}/receive")
    @PreAuthorize("hasAuthority('PURCHASE_WRITE')")
    public String receive(@PathVariable Long id, RedirectAttributes ra) {
        try {
            purchaseService.receive(id);
            ra.addFlashAttribute("success", "Commande réceptionnée, stock mis à jour.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/purchases/" + id;
    }

    @PostMapping("/{id}/pay")
    @PreAuthorize("hasAuthority('PURCHASE_WRITE')")
    public String pay(@PathVariable Long id, @RequestParam Double amount, RedirectAttributes ra) {
        try {
            purchaseService.pay(id, amount);
            ra.addFlashAttribute("success", "Paiement enregistré.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/purchases/" + id;
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('PURCHASE_WRITE')")
    public String cancel(@PathVariable Long id, RedirectAttributes ra) {
        try {
            purchaseService.cancel(id);
            ra.addFlashAttribute("success", "Commande annulée.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/purchases/" + id;
    }
}
