package com.afristock.model.enums;

/**
 * Statut d'une commande fournisseur.
 */
public enum PurchaseStatus {
    /** Commande créée, non encore réceptionnée (stock non impacté). */
    BROUILLON,
    /** Commande réceptionnée : le stock du site a été crédité. */
    RECUE,
    /** Commande annulée. */
    ANNULEE
}
