package com.afristock.model.entity;

import com.afristock.model.enums.CustomerType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;

/**
 * Client d'une entreprise (particulier ou entreprise).
 *
 * <p>Entité « tenant » (isolée par {@code company_id}) et auditée. Gère la fidélité et le plafond
 * de crédit ; l'encours réel et l'historique des achats seront alimentés par le module Ventes
 * (Phase 4).</p>
 */
@Entity
@Table(name = "customers")
@Filter(name = "tenantFilter")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Customer extends Auditable implements TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CustomerType type = CustomerType.PARTICULIER;

    private String email;
    private String phone;
    private String address;
    private String city;

    /** Plafond de crédit autorisé (0 = paiement comptant uniquement). */
    @Column(nullable = false)
    private Double creditLimit = 0.0;

    /** Points de fidélité cumulés. */
    @Column(nullable = false)
    private Integer loyaltyPoints = 0;

    @Column(length = 1000)
    private String notes;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "company_id", nullable = false, updatable = false)
    private Long tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", insertable = false, updatable = false)
    private Company company;

    @Override
    public Long getTenantId() {
        return tenantId;
    }

    @Override
    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }
}
