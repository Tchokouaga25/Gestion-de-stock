package com.afristock.model.entity;

import com.afristock.model.enums.Unit;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;

@Entity
@Table(name = "products")
@Filter(name = "tenantFilter")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Product implements TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String reference;

    @Column(nullable = false)
    private String name;

    private String description;

    private Double purchasePrice;
    private Double salePrice;
    private Integer currentQuantity = 0;
    private Integer minThreshold = 10;

    @Enumerated(EnumType.STRING)
    private Unit unit = Unit.PIECE;

    /** Code-barres principal (EAN/UPC). Le QR code peut être généré à l'affichage à partir de ce code. */
    private String barcode;

    /** URL de l'image du produit (stockage local/objet ; voir Phase 10 pour MinIO/S3). */
    private String imageUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "brand_id")
    private Brand brand;

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
