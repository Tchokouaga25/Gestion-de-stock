package com.afristock.controller;

import com.afristock.model.entity.Category;
import com.afristock.model.entity.Product;
import com.afristock.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/products")
@RequiredArgsConstructor
public class ProductWebController {

    private final ProductService productService;

    // --- LISTE DES PRODUITS ---
    @GetMapping
    public String listProducts(Model model, @RequestParam(value = "search", required = false) String search) {
        if (search != null && !search.isEmpty()) {
            model.addAttribute("products", productService.searchProducts(search));
        } else {
            model.addAttribute("products", productService.getAllProducts());
        }
        return "products/list";
    }

    // --- GESTION DES CATÉGORIES (ADMIN SEULEMENT) ---
    @GetMapping("/categories")
    @PreAuthorize("hasAuthority('ADMIN_PME')")
    public String listCategories(Model model) {
        model.addAttribute("categories", productService.getAllCategories());
        model.addAttribute("newCategory", new Category());
        return "products/categories";
    }

    @PostMapping("/categories/save")
    @PreAuthorize("hasAuthority('ADMIN_PME')")
    public String saveCategory(@ModelAttribute Category category, RedirectAttributes ra) {
        try {
            productService.saveCategory(category);
            ra.addFlashAttribute("success", "Catégorie enregistrée avec succès.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/products/categories";
    }

    // --- FORMULAIRE AJOUT PRODUIT (ADMIN SEULEMENT) ---
    @GetMapping("/add")
    @PreAuthorize("hasAuthority('ADMIN_PME')")
    public String showAddForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", productService.getAllCategories());
        return "products/form";
    }

    @PostMapping("/save")
    @PreAuthorize("hasAuthority('ADMIN_PME')")
    public String saveProduct(@ModelAttribute Product product, RedirectAttributes ra) {
        try {
            productService.saveProduct(product);
            ra.addFlashAttribute("success", "Produit enregistré avec succès.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/products";
    }

    // --- SUPPRESSION (ADMIN SEULEMENT) ---
    @GetMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('ADMIN_PME')")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes ra) {
        try {
            productService.deleteProduct(id);
            ra.addFlashAttribute("success", "Produit supprimé.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/products";
    }
}
