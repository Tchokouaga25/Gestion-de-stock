package com.afristock.model.enums;

/**
 * Moyen de paiement d'une vente.
 */
public enum PaymentMethod {
    ESPECES,
    MOBILE_MONEY,
    CARTE,
    /** Vente à crédit (paiement différé, imputé sur le plafond de crédit du client). */
    CREDIT
}
