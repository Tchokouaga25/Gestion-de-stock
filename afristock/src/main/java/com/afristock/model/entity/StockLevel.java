package com.afristock.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;

/**
 * Niveau de stock d'un produit dans un site donné (stock multi-sites).
 *
 * <p>Unicité (produit, site) : une seule ligne de stock par couple. Entité « tenant » isolée par
 * {@code company_id}.</p>
 */
@Entity
@Table(name = "stock_levels",
        uniqueConstraints = @UniqueConstraint(name = "uk_stock_product_site", columnNames = {"product_id", "site_id"}))
@Filter(name = "tenantFilter")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class StockLevel extends Auditable implements TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    @Column(nullable = false)
    private Integer quantity = 0;

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
