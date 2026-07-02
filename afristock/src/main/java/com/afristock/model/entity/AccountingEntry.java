package com.afristock.model.entity;

import com.afristock.model.enums.AccountType;
import com.afristock.model.enums.EntryType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;

import java.time.LocalDate;

/**
 * Écriture de trésorerie (recette ou dépense) sur un compte (caisse ou banque).
 *
 * <p>Comptabilité simplifiée (mono-entrée) : journal + soldes par compte. La comptabilité en
 * partie double (grand livre/balance normalisés) pourra être ajoutée ultérieurement.</p>
 */
@Entity
@Table(name = "accounting_entries")
@Filter(name = "tenantFilter")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class AccountingEntry extends Auditable implements TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EntryType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AccountType account = AccountType.CAISSE;

    /** Catégorie libre (ex: "Loyer", "Salaires", "Vente", "Fournisseur"). */
    private String category;

    @Column(nullable = false)
    private Double amount;

    @Column(nullable = false)
    private LocalDate entryDate = LocalDate.now();

    private String description;

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
