package com.afristock.model.entity;

import com.afristock.model.enums.SubscriptionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

/**
 * Abonnement courant d'une entreprise à la plateforme.
 *
 * <p>Une entreprise = un abonnement (le plus récent fait foi). Le statut effectif tient compte de la
 * date de fin (voir {@link #isExpired()}).</p>
 */
@Entity
@Table(name = "subscriptions")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Subscription extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false, unique = true)
    private Long companyId;

    /** Nom de l'offre au moment de la souscription (instantané). */
    private String planName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubscriptionStatus status = SubscriptionStatus.ESSAI;

    @Column(nullable = false)
    private LocalDate startDate = LocalDate.now();

    @Column(nullable = false)
    private LocalDate endDate;

    @Transient
    public boolean isExpired() {
        return endDate != null && endDate.isBefore(LocalDate.now());
    }

    /** Statut effectif : EXPIRE si la date de fin est passée, sinon le statut stocké. */
    @Transient
    public SubscriptionStatus getEffectiveStatus() {
        return isExpired() ? SubscriptionStatus.EXPIRE : status;
    }
}
