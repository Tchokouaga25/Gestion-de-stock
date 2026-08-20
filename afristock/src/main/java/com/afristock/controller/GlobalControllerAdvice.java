package com.afristock.controller;

import com.afristock.model.entity.Site;
import com.afristock.model.entity.User;
import com.afristock.repository.SiteRepository;
import com.afristock.repository.UserRepository;
import com.afristock.service.NotificationService;
import com.afristock.service.SubscriptionService;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice
@RequiredArgsConstructor
public class GlobalControllerAdvice {

    /** Clé de session pour le site "actif" choisi via le sélecteur de la sidebar. */
    public static final String CURRENT_SITE_SESSION_KEY = "currentSiteId";

    private final UserRepository userRepository;
    private final NotificationService notificationService;
    private final SiteRepository siteRepository;
    private final SubscriptionService subscriptionService;

    @ModelAttribute
    public void addAttributes(Model model, Authentication authentication, HttpSession session) {
        if (authentication != null && authentication.getPrincipal() instanceof User user) {
            // Charger l'utilisateur avec sa compagnie pour avoir les données fraîches
            User userWithCompany = userRepository.findByIdWithCompany(user.getId());
            model.addAttribute("user", userWithCompany);
            // Le Super-Administrateur n'appartient à aucune entreprise : pas de contexte tenant,
            // donc pas de notifications/sites/abonnement calculés (s'appuient sur TenantContext /
            // company_id, absents pour ce profil).
            if (userWithCompany.getCompany() != null) {
                Long tenantId = userWithCompany.getCompany().getId();
                model.addAttribute("companyName", userWithCompany.getCompany().getName());
                NotificationService.NotificationSummary summary = notificationService.getSummary();
                model.addAttribute("notificationCount", summary.count());
                model.addAttribute("notifications", summary.items());

                // Sélecteur de site (sidebar) : affiché partout via le fragment partagé.
                List<Site> sidebarSites = siteRepository.findByTenantIdOrderByName(tenantId).stream()
                        .filter(Site::isActive)
                        .toList();
                model.addAttribute("sidebarSites", sidebarSites);
                if (!sidebarSites.isEmpty()) {
                    Object sessionSiteId = session.getAttribute(CURRENT_SITE_SESSION_KEY);
                    Site currentSite = sidebarSites.stream()
                            .filter(s -> s.getId().equals(sessionSiteId))
                            .findFirst()
                            .orElseGet(() -> {
                                // Par défaut : le site assigné à l'utilisateur (accès HR site-scopé),
                                // sinon simplement le premier site actif de l'entreprise.
                                Site fallback = userWithCompany.getSite() != null && userWithCompany.getSite().isActive()
                                        ? userWithCompany.getSite() : sidebarSites.get(0);
                                session.setAttribute(CURRENT_SITE_SESSION_KEY, fallback.getId());
                                return fallback;
                            });
                    model.addAttribute("currentSite", currentSite);
                }

                subscriptionService.forCompany(tenantId).ifPresent(sub -> model.addAttribute("currentSubscription", sub));
            }
        }
    }
}
