package com.afristock.controller;

import com.afristock.model.entity.Supplier;
import com.afristock.service.SupplierService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @GetMapping
    @PreAuthorize("hasAuthority('SUPPLIER_READ')")
    public String list(Model model, @RequestParam(value = "search", required = false) String search) {
        if (search != null && !search.isBlank()) {
            model.addAttribute("suppliers", supplierService.search(search));
        } else {
            model.addAttribute("suppliers", supplierService.getAll());
        }
        return "suppliers/list";
    }

    @GetMapping("/add")
    @PreAuthorize("hasAuthority('SUPPLIER_WRITE')")
    public String showAddForm(Model model) {
        model.addAttribute("supplier", new Supplier());
        return "suppliers/form";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('SUPPLIER_WRITE')")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("supplier", supplierService.getById(id));
            return "suppliers/form";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/suppliers";
        }
    }

    @PostMapping("/save")
    @PreAuthorize("hasAuthority('SUPPLIER_WRITE')")
    public String save(@ModelAttribute Supplier supplier, RedirectAttributes ra) {
        try {
            supplierService.save(supplier);
            ra.addFlashAttribute("success", "Fournisseur enregistré avec succès.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/suppliers";
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('SUPPLIER_DELETE')")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            supplierService.delete(id);
            ra.addFlashAttribute("success", "Fournisseur supprimé.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/suppliers";
    }
}
