package com.afristock.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;

/**
 * Marque de produit (ex: "Coca-Cola", "Nestlé").
 *
 * <p>Entité « tenant » isolée par {@code company_id}.</p>
 */
@Entity
@Table(name = "brands")
@Filter(name = "tenantFilter")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Brand extends Auditable implements TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

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
