package com.afristock.controller;

import com.afristock.model.entity.User;
import com.afristock.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * Vue « Mon abonnement » côté entreprise cliente.
 */
@Controller
@RequestMapping("/subscription")
@RequiredArgsConstructor
public class SubscriptionController {

    private final SubscriptionService subscriptionService;

    @GetMapping
    @PreAuthorize("hasAuthority('COMPANY_VIEW_SETTINGS')")
    public String mySubscription(Authentication authentication, Model model) {
        User user = (User) authentication.getPrincipal();
        if (user.getCompany() != null) {
            subscriptionService.forCompany(user.getCompany().getId())
                    .ifPresent(s -> model.addAttribute("subscription", s));
        }
        return "subscription/view";
    }
}
