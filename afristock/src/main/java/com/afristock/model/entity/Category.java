package com.afristock.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;

@Entity
@Table(name = "categories")
@Filter(name = "tenantFilter")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Category implements TenantAware {

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
