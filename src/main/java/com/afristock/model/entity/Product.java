package com.afristock.model.entity;

import com.afristock.model.enums.Unit;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

@Entity
@Table(name = "products")
@FilterDef(name = "productTenantFilter", parameters = @ParamDef(name = "tenantId", type = Long.class))
@Filter(name = "productTenantFilter", condition = "company_id = :tenantId")
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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

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
