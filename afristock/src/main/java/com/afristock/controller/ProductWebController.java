package com.afristock.controller;

import com.afristock.model.entity.Brand;
import com.afristock.model.entity.Category;
import com.afristock.model.entity.Product;
import com.afristock.model.entity.ProductVariant;
import com.afristock.model.enums.Unit;
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
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public String listProducts(Model model, @RequestParam(value = "search", required = false) String search) {
        if (search != null && !search.isEmpty()) {
            model.addAttribute("products", productService.searchProducts(search));
        } else {
            model.addAttribute("products", productService.getAllProducts());
        }
        return "products/list";
    }

    // --- CATÉGORIES ---
    @GetMapping("/categories")
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    public String listCategories(Model model) {
        model.addAttribute("categories", productService.getAllCategories());
        model.addAttribute("newCategory", new Category());
        return "products/categories";
    }

    @PostMapping("/categories/save")
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    public String saveCategory(@ModelAttribute Category category, RedirectAttributes ra) {
        try {
            productService.saveCategory(category);
            ra.addFlashAttribute("success", "Catégorie enregistrée avec succès.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/products/categories";
    }

    @PostMapping("/categories/delete/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    public String deleteCategory(@PathVariable Long id, RedirectAttributes ra) {
        try {
            productService.deleteCategory(id);
            ra.addFlashAttribute("success", "Catégorie supprimée.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/products/categories";
    }

    // --- MARQUES ---
    @GetMapping("/brands")
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    public String listBrands(Model model) {
        model.addAttribute("brands", productService.getAllBrands());
        model.addAttribute("newBrand", new Brand());
        return "products/brands";
    }

    @PostMapping("/brands/save")
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    public String saveBrand(@ModelAttribute Brand brand, RedirectAttributes ra) {
        try {
            productService.saveBrand(brand);
            ra.addFlashAttribute("success", "Marque enregistrée avec succès.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/products/brands";
    }

    @PostMapping("/brands/delete/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    public String deleteBrand(@PathVariable Long id, RedirectAttributes ra) {
        try {
            productService.deleteBrand(id);
            ra.addFlashAttribute("success", "Marque supprimée.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/products/brands";
    }

    // --- FORMULAIRE PRODUIT (AJOUT / ÉDITION) ---
    @GetMapping("/add")
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    public String showAddForm(Model model) {
        model.addAttribute("product", new Product());
        populateFormLists(model);
        return "products/form";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("product", productService.getProduct(id));
            populateFormLists(model);
            return "products/form";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/products";
        }
    }

    @PostMapping("/save")
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    public String saveProduct(@ModelAttribute Product product,
                              @RequestParam Long categoryId,
                              @RequestParam(required = false) Long brandId,
                              RedirectAttributes ra) {
        try {
            productService.saveProduct(product, categoryId, brandId);
            ra.addFlashAttribute("success", "Produit enregistré avec succès.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/products";
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_DELETE')")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes ra) {
        try {
            productService.deleteProduct(id);
            ra.addFlashAttribute("success", "Produit supprimé.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/products";
    }

    // --- VARIANTES D'UN PRODUIT ---
    @GetMapping("/{id}/variants")
    @PreAuthorize("hasAuthority('PRODUCT_READ')")
    public String listVariants(@PathVariable Long id, Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("product", productService.getProduct(id));
            model.addAttribute("variants", productService.getVariants(id));
            model.addAttribute("newVariant", new ProductVariant());
            return "products/variants";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/products";
        }
    }

    @PostMapping("/{id}/variants/save")
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    public String addVariant(@PathVariable Long id, @ModelAttribute ProductVariant variant, RedirectAttributes ra) {
        try {
            productService.addVariant(id, variant);
            ra.addFlashAttribute("success", "Variante ajoutée.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/products/" + id + "/variants";
    }

    @PostMapping("/{id}/variants/delete/{variantId}")
    @PreAuthorize("hasAuthority('PRODUCT_WRITE')")
    public String deleteVariant(@PathVariable Long id, @PathVariable Long variantId, RedirectAttributes ra) {
        try {
            productService.deleteVariant(variantId);
            ra.addFlashAttribute("success", "Variante supprimée.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/products/" + id + "/variants";
    }

    private void populateFormLists(Model model) {
        model.addAttribute("categories", productService.getAllCategories());
        model.addAttribute("brands", productService.getAllBrands());
        model.addAttribute("units", Unit.values());
    }
}
