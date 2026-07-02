package com.afristock.model.enums;

/**
 * Statut d'une entreprise cliente sur la plateforme SaaS.
 */
public enum CompanyStatus {
    /** Entreprise active : ses utilisateurs peuvent se connecter et travailler. */
    ACTIVE,
    /** Entreprise suspendue par le Super-Administrateur : connexion bloquée. */
    SUSPENDED
}
