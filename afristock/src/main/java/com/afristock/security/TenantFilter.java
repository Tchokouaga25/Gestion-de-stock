package com.afristock.security;

import com.afristock.security.TenantContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TenantFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();

            if (auth != null && auth.isAuthenticated() && auth.getPrincipal() instanceof com.afristock.model.entity.User user
                    && user.getCompany() != null) {
                // Utilisateur d'une entreprise cliente : on fixe son tenant.
                Long tenantId = user.getCompany().getId();
                TenantContext.setCurrentTenant(tenantId);
            } else {
                // Endpoints publics (register/login) OU Super-Administrateur (sans entreprise) :
                // pas de tenant → aucun filtrage par company_id.
                // Pour les endpoints publics (ex: register), on peut mettre null ou un tenant par défaut
                TenantContext.setCurrentTenant(null);
            }

            filterChain.doFilter(request, response);
        } finally {
            TenantContext.clear(); // Toujours nettoyer !
        }
    }
}