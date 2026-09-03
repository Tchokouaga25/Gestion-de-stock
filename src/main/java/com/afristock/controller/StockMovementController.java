package com.afristock.controller;

import com.afristock.model.entity.Product;
import com.afristock.model.enums.MovementType;
import com.afristock.service.ProductService;
import com.afristock.service.StockService;
import lombok.RequiredArgsConstructor;
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

    @GetMapping("/new")
    public String showMovementForm(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("types", MovementType.values());
        return "movements/form";
    }

    @PostMapping("/save")
    public String saveMovement(@RequestParam Long productId,
                               @RequestParam Integer quantity,
                               @RequestParam MovementType type,
                               @RequestParam String reason,
                               RedirectAttributes ra) {
        try {
            stockService.recordMovement(productId, quantity, type, reason);
            ra.addFlashAttribute("success", "Mouvement enregistré avec succès.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/movements/new";
        }
        return "redirect:/dashboard";
    }
}
