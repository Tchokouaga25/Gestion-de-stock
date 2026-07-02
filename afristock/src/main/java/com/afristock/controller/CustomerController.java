package com.afristock.controller;

import com.afristock.model.entity.Customer;
import com.afristock.model.enums.CustomerType;
import com.afristock.service.CustomerService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    @PreAuthorize("hasAuthority('CUSTOMER_READ')")
    public String list(Model model, @RequestParam(value = "search", required = false) String search) {
        if (search != null && !search.isBlank()) {
            model.addAttribute("customers", customerService.search(search));
        } else {
            model.addAttribute("customers", customerService.getAll());
        }
        return "customers/list";
    }

    @GetMapping("/add")
    @PreAuthorize("hasAuthority('CUSTOMER_WRITE')")
    public String showAddForm(Model model) {
        model.addAttribute("customer", new Customer());
        model.addAttribute("types", CustomerType.values());
        return "customers/form";
    }

    @GetMapping("/edit/{id}")
    @PreAuthorize("hasAuthority('CUSTOMER_WRITE')")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes ra) {
        try {
            model.addAttribute("customer", customerService.getById(id));
            model.addAttribute("types", CustomerType.values());
            return "customers/form";
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
            return "redirect:/customers";
        }
    }

    @PostMapping("/save")
    @PreAuthorize("hasAuthority('CUSTOMER_WRITE')")
    public String save(@ModelAttribute Customer customer, RedirectAttributes ra) {
        try {
            customerService.save(customer);
            ra.addFlashAttribute("success", "Client enregistré avec succès.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/customers";
    }

    @PostMapping("/delete/{id}")
    @PreAuthorize("hasAuthority('CUSTOMER_DELETE')")
    public String delete(@PathVariable Long id, RedirectAttributes ra) {
        try {
            customerService.delete(id);
            ra.addFlashAttribute("success", "Client supprimé.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/customers";
    }
}
