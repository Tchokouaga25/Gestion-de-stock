package com.afristock.model.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Offre d'abonnement de la plateforme SaaS (globale, gérée par le Super-Administrateur).
 *
 * <p>Non « tenant » : partagée par toutes les entreprises.</p>
 */
@Entity
@Table(name = "subscription_plans")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class SubscriptionPlan extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    /** Prix mensuel (FCFA). 0 pour l'offre d'essai. */
    @Column(nullable = false)
    private Double monthlyPrice = 0.0;

    /** Nombre maximum d'utilisateurs autorisés (null = illimité). */
    private Integer maxUsers;

    private String description;

    @Column(nullable = false)
    private boolean active = true;
}
