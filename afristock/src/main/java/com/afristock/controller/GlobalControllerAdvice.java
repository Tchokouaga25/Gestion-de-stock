package com.afristock.controller;

import com.afristock.model.entity.User;
import com.afristock.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    private final UserRepository userRepository;

    @ModelAttribute
    public void addAttributes(Model model, Authentication authentication) {
        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            // Charger l'utilisateur avec sa compagnie pour avoir les données fraîches
            User userWithCompany = userRepository.findByIdWithCompany(user.getId());
            model.addAttribute("user", userWithCompany);
            // Le Super-Administrateur n'appartient à aucune entreprise.
            if (userWithCompany.getCompany() != null) {
                model.addAttribute("companyName", userWithCompany.getCompany().getName());
            }
        }
    }
}
