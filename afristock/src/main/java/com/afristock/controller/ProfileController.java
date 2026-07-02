package com.afristock.controller;

import com.afristock.model.entity.User;
import com.afristock.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final UserRepository userRepository;

    @GetMapping
    public String showProfile(Model model, Authentication authentication) {
        User user = (User) authentication.getPrincipal();
        // On recharge l'utilisateur pour avoir les dernières infos
        model.addAttribute("profileUser", userRepository.findById(user.getId()).orElseThrow());
        return "profile/index";
    }

    @PostMapping("/update")
    public String updateProfile(@ModelAttribute("profileUser") User profileUser, RedirectAttributes ra) {
        try {
            User existingUser = userRepository.findById(profileUser.getId()).orElseThrow();
            existingUser.setFirstName(profileUser.getFirstName());
            existingUser.setLastName(profileUser.getLastName());
            existingUser.setEmail(profileUser.getEmail());
            
            userRepository.save(existingUser);
            ra.addFlashAttribute("success", "Profil mis à jour avec succès.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", "Erreur lors de la mise à jour : " + e.getMessage());
        }
        return "redirect:/profile";
    }
}
