package com.afristock.model.entity;

import com.afristock.model.enums.MovementType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;
import java.time.LocalDateTime;

@Entity
@Table(name = "stock_movements")
@Filter(name = "tenantFilter")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class StockMovement implements TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    // Site concerné par le mouvement (le stock est désormais géré par site via StockLevel).
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id")
    private Site site;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MovementType type; // ENTRY, EXIT, ADJUSTMENT

    @Column(nullable = false)
    private Integer quantity;

    private String reason;

    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "company_id", nullable = false, updatable = false)
    private Long tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", insertable = false, updatable = false)
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user; // L'utilisateur qui a fait le mouvement

    @Override
    public Long getTenantId() { return tenantId; }
    @Override
    public void setTenantId(Long tenantId) { this.tenantId = tenantId; }
}
