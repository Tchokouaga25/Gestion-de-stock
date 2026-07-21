package com.afristock.controller;

import com.afristock.model.entity.Site;
import com.afristock.model.entity.User;
import com.afristock.model.enums.Role;
import com.afristock.repository.SiteRepository;
import com.afristock.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN_PME')")
public class AdminUserController {

    private final UserRepository userRepository;
    private final SiteRepository siteRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public String listUsers(Model model, Authentication authentication) {
        User currentUser = (User) authentication.getPrincipal();
        // Filtrer les utilisateurs de la même compagnie
        List<User> colleagues = userRepository.findAll().stream()
                .filter(u -> u.getCompany() != null && u.getCompany().getId().equals(currentUser.getCompany().getId()))
                .toList();

        model.addAttribute("users", colleagues);
        model.addAttribute("newUser", new User());
        model.addAttribute("roles", Role.values());
        model.addAttribute("user", currentUser);
        model.addAttribute("companyName", currentUser.getCompany().getName());
        model.addAttribute("sites", siteRepository.findByTenantIdOrderByName(currentUser.getCompany().getId()));
        return "admin/users";
    }

    @PostMapping("/save")
    public String saveUser(@ModelAttribute User newUser, Authentication authentication, RedirectAttributes ra) {
        try {
            User currentUser = (User) authentication.getPrincipal();
            
            // Sécurité SaaS : On lie l'utilisateur à la compagnie de l'admin
            newUser.setCompany(currentUser.getCompany());
            newUser.setPassword(passwordEncoder.encode("Afristock123")); // Mot de passe par défaut
            
            userRepository.save(newUser);
            ra.addFlashAttribute("success", "Collaborateur invité avec succès. Mot de passe par défaut : Afristock123");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    /**
     * Affecte (ou retire, si {@code siteId} est absent) le site de responsabilité d'un
     * collaborateur — permet un accès en lecture seule à la page « Personnel » de ce site
     * (voir {@code SitePersonnelController}). Endpoint volontairement séparé de {@link #saveUser}
     * : celui-ci lie le formulaire via {@code @ModelAttribute User newUser}, dont l'attribut
     * modèle résolu ({@code "user"}) entre en collision avec l'utilisateur connecté injecté par
     * {@link GlobalControllerAdvice} — un pattern déjà responsable d'un bug d'écrasement de
     * compte ailleurs dans l'application. On ne rajoute donc pas de champ site à ce formulaire.
     */
    @PostMapping("/{id}/site")
    public String assignSite(@PathVariable Long id, @RequestParam(required = false) Long siteId,
                              Authentication authentication, RedirectAttributes ra) {
        try {
            User currentUser = (User) authentication.getPrincipal();
            User target = userRepository.findById(id)
                    .filter(u -> u.getCompany() != null && u.getCompany().getId().equals(currentUser.getCompany().getId()))
                    .orElseThrow(() -> new IllegalArgumentException("Collaborateur introuvable."));
            if (siteId != null) {
                Site site = siteRepository.findById(siteId)
                        .filter(s -> s.getTenantId().equals(currentUser.getCompany().getId()))
                        .orElseThrow(() -> new IllegalArgumentException("Site introuvable."));
                target.setSite(site);
            } else {
                target.setSite(null);
            }
            userRepository.save(target);
            ra.addFlashAttribute("success", "Site du collaborateur mis à jour.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur : " + e.getMessage());
        }
        return "redirect:/admin/users";
    }
}
