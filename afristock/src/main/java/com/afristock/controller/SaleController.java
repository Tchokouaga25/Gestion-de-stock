package com.afristock.controller;

import com.afristock.model.enums.PaymentMethod;
import com.afristock.model.enums.SaleType;
import com.afristock.service.CustomerService;
import com.afristock.service.ProductService;
import com.afristock.service.SaleService;
import com.afristock.service.SiteService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

/**
 * Ventes & caisse (Phase 4).
 */
@Controller
@RequestMapping("/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;
    private final ProductService productService;
    private final SiteService siteService;
    private final CustomerService customerService;

    @GetMapping
    @PreAuthorize("hasAuthority('SALE_READ')")
    public String list(@RequestParam(defaultValue = "0") int page, Model model) {
        model.addAttribute("page", saleService.getPage(page, 20));
        return "sales/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasAuthority('SALE_WRITE')")
    public String newSale(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("sites", siteService.getAll());
        model.addAttribute("customers", customerService.getAll());
        model.addAttribute("saleTypes", SaleType.values());
        model.addAttribute("paymentMethods", PaymentMethod.values());
        return "sales/new";
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SALE_WRITE')")
    public String create(@RequestParam Long siteId,
                         @RequestParam(required = false) Long customerId,
                         @RequestParam SaleType saleType,
                         @RequestParam PaymentMethod paymentMethod,
                         @RequestParam(required = false) Double amountPaid,
                         @RequestParam(name = "productId", required = false) List<Long> productIds,
                         @RequestParam(name = "quantity", required = false) List<Integer> quantities,
                         RedirectAttributes ra) {
        try {
            var sale = saleService.createSale(siteId, customerId, saleType, paymentMethod,
                    amountPaid, productIds, quantities);
            ra.addFlashAttribute("success", "Vente " + sale.getReference() + " enregistrée.");
            return "redirect:/sales/" + sale.getId();
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/sales/new";
        }
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SALE_READ')")
    public String receipt(@PathVariable Long id, Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("sale", saleService.getReceipt(id));
            return "sales/receipt";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/sales";
        }
    }

    @GetMapping("/cash")
    @PreAuthorize("hasAuthority('SALE_READ')")
    public String cash(Model model) {
        model.addAttribute("summary", saleService.getTodayCashSummary());
        return "sales/cash";
    }
}
