package com.afristock.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;

/**
 * Variante d'un produit (ex: taille « 500ml », couleur « Rouge »).
 *
 * <p>Chaque variante peut avoir son propre code-barres et un ajustement de prix par rapport au
 * produit parent. Entité « tenant » isolée par {@code company_id}.</p>
 */
@Entity
@Table(name = "product_variants")
@Filter(name = "tenantFilter")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class ProductVariant extends Auditable implements TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    /** Libellé de la variante (ex: "500ml", "Taille L", "Rouge"). */
    @Column(nullable = false)
    private String name;

    private String barcode;

    /** Différence de prix de vente par rapport au produit parent (peut être négative). */
    @Column(nullable = false)
    private Double priceAdjustment = 0.0;

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
