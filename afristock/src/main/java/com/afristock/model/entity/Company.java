package com.afristock.model.entity;

import com.afristock.model.enums.CompanyStatus;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

//Représenter une entreprise cliente du SaaS.
//C’est le tenant lui-même.
//Chaque entreprise qui utilise AfriStock aura une entrée dans cette table.

@Entity
@Table(name = "companies")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;
    private String city;
    private String logoUrl;
    private String sector;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CompanyStatus status = CompanyStatus.ACTIVE;

    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime updatedAt;

    public boolean isActive() {
        return status == CompanyStatus.ACTIVE;
    }
}