package com.afristock.model.enums;

/**
 * Permissions fines utilisées pour le contrôle d'accès (@PreAuthorize).
 *
 * <p>Chaque {@link Role} accorde un sous-ensemble de ces permissions
 * (voir {@link Role#getPermissions()}). On évite ainsi de coder en dur les rôles dans les
 * contrôleurs : on protège plutôt par capacité métier.</p>
 */
public enum Permission {

    // --- Plateforme (réservé au Super-Administrateur) ---
    PLATFORM_MANAGE_COMPANIES,
    PLATFORM_MANAGE_FEATURES,
    PLATFORM_VIEW_STATS,

    // --- Administration de l'entreprise ---
    COMPANY_MANAGE_USERS,
    COMPANY_VIEW_SETTINGS,

    // --- Sites (boutiques / entrepôts) ---
    SITE_READ,
    SITE_WRITE,
    SITE_DELETE,

    // --- Fournisseurs ---
    SUPPLIER_READ,
    SUPPLIER_WRITE,
    SUPPLIER_DELETE,

    // --- Clients ---
    CUSTOMER_READ,
    CUSTOMER_WRITE,
    CUSTOMER_DELETE,

    // --- Produits & catégories ---
    PRODUCT_READ,
    PRODUCT_WRITE,
    PRODUCT_DELETE,

    // --- Stock ---
    STOCK_READ,
    STOCK_WRITE
}
