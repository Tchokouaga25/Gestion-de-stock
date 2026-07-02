package com.afristock.model.entity;

import com.afristock.model.enums.ContractType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Filter;

import java.time.LocalDate;

/**
 * Employé d'une entreprise. Profil RH : contrat, salaire de base, affectation à un site.
 */
@Entity
@Table(name = "employees")
@Filter(name = "tenantFilter")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Employee extends Auditable implements TenantAware {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String firstName;

    @Column(nullable = false)
    private String lastName;

    private String position;

    @Enumerated(EnumType.STRING)
    private ContractType contractType = ContractType.CDI;

    private LocalDate hireDate;

    private Double baseSalary;

    private String phone;
    private String email;

    /** Site d'affectation (facultatif). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id")
    private Site site;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "company_id", nullable = false, updatable = false)
    private Long tenantId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id", insertable = false, updatable = false)
    private Company company;

    @Transient
    public String getFullName() {
        return firstName + " " + lastName;
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
