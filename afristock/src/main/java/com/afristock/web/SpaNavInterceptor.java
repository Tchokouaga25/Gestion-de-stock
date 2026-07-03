package com.afristock.web;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Permet une navigation "SPA-like" sans dépendance JS externe : quand une requête porte
 * l'en-tête {@code X-Spa-Nav}, seule la fragment Thymeleaf {@code :: content} de la vue est
 * rendue (voir {@code static/js/spa-nav.js}), au lieu de la page complète.
 *
 * <p>Injecte aussi {@code activeNav} dans le modèle pour que la sidebar partagée
 * ({@code templates/fragments/layout.html}) sache quel lien surligner, sans dépendre de
 * {@code #httpServletRequest} (retiré des expressions Thymeleaf depuis la 3.1).</p>
 */
@Component
public class SpaNavInterceptor implements HandlerInterceptor {

    private static final String SPA_NAV_HEADER = "X-Spa-Nav";

    /** Préfixe d'URL -> clé de navigation utilisée par la sidebar. Les préfixes les plus longs
     *  gagnent (ex: /products/categories avant /products) pour éviter les collisions. */
    private static final Map<String, String> NAV_PREFIXES = new LinkedHashMap<>();
    static {
        NAV_PREFIXES.put("/dashboard", "dashboard");
        NAV_PREFIXES.put("/sites", "sites");
        NAV_PREFIXES.put("/suppliers", "suppliers");
        NAV_PREFIXES.put("/customers", "customers");
        NAV_PREFIXES.put("/products/categories", "admin-categories");
        NAV_PREFIXES.put("/products", "products");
        NAV_PREFIXES.put("/sales", "sales");
        NAV_PREFIXES.put("/purchases", "purchases");
        NAV_PREFIXES.put("/stock", "stock");
        NAV_PREFIXES.put("/movements", "movements");
        NAV_PREFIXES.put("/profile", "profile");
        NAV_PREFIXES.put("/admin/users", "admin-users");
        NAV_PREFIXES.put("/subscription", "subscription");
        NAV_PREFIXES.put("/accounting", "accounting");
        NAV_PREFIXES.put("/hr", "hr");
        NAV_PREFIXES.put("/reports", "reports");
        NAV_PREFIXES.put("/super-admin/companies", "superadmin-companies");
        NAV_PREFIXES.put("/super-admin/plans", "superadmin-plans");
        NAV_PREFIXES.put("/super-admin/features", "superadmin-features");
    }

    static String computeActiveNav(String uri) {
        return NAV_PREFIXES.entrySet().stream()
                .filter(e -> uri.equals(e.getKey()) || uri.startsWith(e.getKey() + "/"))
                .max(Comparator.comparingInt(e -> e.getKey().length()))
                .map(Map.Entry::getValue)
                .orElse("");
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response,
                            Object handler, ModelAndView mv) {
        if (mv == null || !mv.hasView()) {
            return;
        }
        String viewName = mv.getViewName();
        if (viewName == null || viewName.startsWith("redirect:") || viewName.startsWith("forward:")
                || viewName.contains("::")) {
            return;
        }

        mv.addObject("currentUri", request.getRequestURI());
        mv.addObject("activeNav", computeActiveNav(request.getRequestURI()));

        if ("true".equals(request.getHeader(SPA_NAV_HEADER))) {
            mv.setViewName(viewName + " :: content");
        }
    }
}
