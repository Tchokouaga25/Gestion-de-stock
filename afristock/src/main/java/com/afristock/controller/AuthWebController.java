package com.afristock.controller;


import com.afristock.dto.RegisterRequest;
import com.afristock.service.RegistrationService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthWebController {

    private final RegistrationService registrationService;

    // Affiche le formulaire d'inscription
    @GetMapping("/register")
    public String showRegisterForm(Model model, HttpServletRequest request) {
        // La page est longue : sans session déjà ouverte, Tomcat committe la réponse
        // (dépassement du buffer) avant que Thymeleaf ne puisse créer la session
        // nécessaire au token CSRF du formulaire, ce qui tronque la page. On force
        // donc la création de la session avant que le rendu ne commence.
        request.getSession();
        model.addAttribute("registerRequest", new RegisterRequest());
        return "auth/register";  // → templates/auth/accueil.html
    }

    // Traite la soumission du formulaire (POST)
    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("registerRequest") RegisterRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "auth/register";  // revient sur le formulaire avec les erreurs
        }

        try {
            registrationService.registerCompanyAndAdmin(request);
            redirectAttributes.addFlashAttribute("success",
                    "Inscription réussie ! Vous pouvez maintenant vous connecter.");
            return "redirect:/auth/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "auth/register";
        }
    }

    // Dans le même AuthWebController

    @GetMapping("/login")
    public String showLoginForm(Model model,
                                @RequestParam(value = "error", required = false) String error,
                                @RequestParam(value = "logout", required = false) String logout) {

        if (error != null) {
            model.addAttribute("error", "Identifiants incorrects. Veuillez réessayer.");
        }
        if (logout != null) {
            model.addAttribute("message", "Vous avez été déconnecté avec succès.");
        }

        return "auth/login";  // → templates/auth/login.html
    }
}