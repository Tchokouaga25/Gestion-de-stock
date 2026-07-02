package com.afristock.model.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;

import java.time.LocalDateTime;

/**
 * Transfert de stock d'un produit entre deux sites (ex: entrepôt → boutique).
 *
 * <p>Trace l'opération ; la mise à jour des quantités se fait sur les {@link StockLevel}
 * correspondants. Entité « tenant » isolée par {@code company_id}.</p>
 */
@Entity
@Table(name = "stock_transfers")
@Filter(name = "tenantFilter")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class StockTransfer extends Auditable implements TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_site_id", nullable = false)
    private Site fromSite;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_site_id", nullable = false)
    private Site toSite;

    @Column(nullable = false)
    private Integer quantity;

    private String reason;

    private LocalDateTime transferredAt = LocalDateTime.now();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

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
