package com.afristock.model.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Fonctionnalité de la plateforme pilotée par un « feature flag » global.
 *
 * <p>Gérée uniquement par le Super-Administrateur. Lorsqu'une fonctionnalité est désactivée
 * ({@code enabled = false}), elle disparaît chez toutes les entreprises clientes (menus masqués,
 * accès refusé). C'est un drapeau <b>global</b> (pas par entreprise), donc non soumis au filtre
 * multi-tenant.</p>
 */
@Entity
@Table(name = "features")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Feature extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Clé technique stable utilisée dans le code et les templates (ex: "PRODUCTS", "REPORTS"). */
    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    private String description;

    /** Catégorie d'affichage dans la page Fonctionnalités (ex: "Ventes", "Stock", "RH"). */
    private String category;

    @Column(nullable = false)
    private boolean enabled = true;
}
