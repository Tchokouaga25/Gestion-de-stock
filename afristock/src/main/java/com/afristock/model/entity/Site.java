package com.afristock.model.entity;

import com.afristock.model.enums.SiteType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;

/**
 * Site physique d'une entreprise : boutique ou entrepôt.
 *
 * <p>Entité « tenant » : isolée par {@code company_id} via le filtre {@code tenantFilter}.
 * C'est le socle du stock multi-sites, des transferts et des ventes (Phases 3-4).</p>
 */
@Entity
@Table(name = "sites")
@Filter(name = "tenantFilter")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Site extends Auditable implements TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SiteType type = SiteType.BOUTIQUE;

    private String address;
    private String city;
    private String phone;

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
