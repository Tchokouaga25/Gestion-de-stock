package com.afristock.model.enums;

import java.util.Set;

import static com.afristock.model.enums.Permission.*;

/**
 * Rôles utilisateur.
 *
 * <ul>
 *   <li>{@link #SUPER_ADMIN} : exploitant de la plateforme SaaS (espace dédié, voit toutes les
 *       entreprises). N'appartient pas au périmètre d'une entreprise cliente.</li>
 *   <li>{@link #ADMIN_PME} : administrateur d'une entreprise cliente.</li>
 *   <li>{@link #USER_PME} : employé d'une entreprise cliente.</li>
 * </ul>
 *
 * <p>Chaque rôle porte son ensemble de {@link Permission}. {@code User.getAuthorities()} expose à la
 * fois le rôle ({@code ROLE_*}) et ses permissions, ce qui permet d'écrire
 * {@code @PreAuthorize("hasAuthority('PRODUCT_WRITE')")}.</p>
 */
public enum Role {

    SUPER_ADMIN(Set.of(
            PLATFORM_MANAGE_COMPANIES,
            PLATFORM_MANAGE_FEATURES,
            PLATFORM_VIEW_STATS
    )),

    ADMIN_PME(Set.of(
            COMPANY_MANAGE_USERS,
            COMPANY_VIEW_SETTINGS,
            SITE_READ, SITE_WRITE, SITE_DELETE,
            SUPPLIER_READ, SUPPLIER_WRITE, SUPPLIER_DELETE,
            CUSTOMER_READ, CUSTOMER_WRITE, CUSTOMER_DELETE,
            PRODUCT_READ, PRODUCT_WRITE, PRODUCT_DELETE,
            STOCK_READ, STOCK_WRITE
    )),

    USER_PME(Set.of(
            SITE_READ,
            SUPPLIER_READ,
            CUSTOMER_READ, CUSTOMER_WRITE,
            PRODUCT_READ,
            STOCK_READ, STOCK_WRITE
    ));

    private final Set<Permission> permissions;

    Role(Set<Permission> permissions) {
        this.permissions = permissions;
    }

    public Set<Permission> getPermissions() {
        return permissions;
    }
}
