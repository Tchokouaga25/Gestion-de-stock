package com.afristock.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;

/**
 * Fournisseur d'une entreprise.
 *
 * <p>Entité « tenant » (isolée par {@code company_id}) et auditée. La fiche de base ; les dettes et
 * paiements seront gérés par le module Achats (Phase 5).</p>
 */
@Entity
@Table(name = "suppliers")
@Filter(name = "tenantFilter")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Supplier extends Auditable implements TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    /** Personne à contacter chez le fournisseur. */
    private String contactName;

    private String email;
    private String phone;
    private String address;
    private String city;

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
