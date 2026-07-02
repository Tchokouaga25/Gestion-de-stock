package com.afristock.controller;

import com.afristock.model.entity.User;
import com.afristock.repository.ProductRepository;
import com.afristock.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    @GetMapping
    public String dashboard(Model model, Authentication authentication) {

        // Récupérer l'utilisateur connecté
        User user = (User) authentication.getPrincipal();

        // Charger l'utilisateur avec sa compagnie
        User userWithCompany = userRepository.findByIdWithCompany(user.getId());

        // Le Super-Administrateur n'a pas d'entreprise : on le dirige vers son espace dédié.
        if (userWithCompany.getCompany() == null) {
            return "redirect:/super-admin/companies";
        }
        Long tenantId = userWithCompany.getCompany().getId();

        // Statistiques réelles
        long totalProducts = productRepository.findByTenantId(tenantId).size();
        
        model.addAttribute("user", userWithCompany);
        model.addAttribute("companyName", userWithCompany.getCompany().getName());
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("welcomeMessage",
                "Bienvenue sur votre tableau de bord, " + userWithCompany.getFirstName());

        return "dashboard/index";
    }
}
