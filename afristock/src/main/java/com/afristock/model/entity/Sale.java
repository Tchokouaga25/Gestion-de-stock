package com.afristock.model.entity;

import com.afristock.model.enums.PaymentMethod;
import com.afristock.model.enums.SaleType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Vente (facture) réalisée sur un site.
 *
 * <p>Entité « tenant » isolée par {@code company_id}. Les lignes ({@link SaleItem}) sont gérées en
 * cascade. La sortie de stock est enregistrée via des mouvements par site (Phase 3).</p>
 */
@Entity
@Table(name = "sales")
@Filter(name = "tenantFilter")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Sale extends Auditable implements TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Numéro de facture lisible, unique par entreprise (ex: "FAC-000001"). */
    @Column(nullable = false)
    private String reference;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id", nullable = false)
    private Site site;

    /** Client (facultatif : vente comptoir anonyme possible). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id")
    private Customer customer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SaleType saleType = SaleType.DETAIL;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod = PaymentMethod.ESPECES;

    private LocalDateTime saleDate = LocalDateTime.now();

    @Column(nullable = false)
    private Double totalAmount = 0.0;

    /** Montant effectivement encaissé (peut être inférieur au total pour une vente à crédit). */
    @Column(nullable = false)
    private Double amountPaid = 0.0;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SaleItem> items = new ArrayList<>();

    @Column(name = "company_id", nullable = false, updatable = false)
    private Long tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", insertable = false, updatable = false)
    private Company company;

    /** Reste à payer (0 si soldé). */
    @Transient
    public double getBalanceDue() {
        return Math.max(0.0, totalAmount - amountPaid);
    }

    public void addItem(SaleItem item) {
        item.setSale(this);
        item.setTenantId(this.tenantId);
        this.items.add(item);
    }

    @Override
    public Long getTenantId() {
        return tenantId;
    }

    @Override
    public void setTenantId(Long tenantId) {
        this.tenantId = tenantId;
    }
}
