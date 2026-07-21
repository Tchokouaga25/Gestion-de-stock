package com.afristock.model.entity;

//Représenter un utilisateur qui se connecte à l'application.
//login, authentification, autorisations

import com.afristock.model.enums.CompanyStatus;
import com.afristock.model.enums.Role;
import jakarta.persistence.*;
import lombok.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Entity
@Table(name = "users")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor
public class User implements UserDetails {  // pour Spring Security

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    private String firstName;
    private String lastName;

    @Enumerated(EnumType.STRING)
    private Role role = Role.ADMIN_PME;  // enum Role { ADMIN_PME, USER_PME }

    // Nullable : le Super-Administrateur de la plateforme n'appartient à aucune entreprise.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    // Nullable : seuls les collaborateurs affectés à un site précis (ex: responsable de
    // boutique) l'ont ; un admin ou un utilisateur non affecté n'a pas de site.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "site_id")
    private Site site;

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        List<GrantedAuthority> authorities = new ArrayList<>();
        // Le rôle (préfixé ROLE_ pour hasRole(...)) ...
        authorities.add(new SimpleGrantedAuthority("ROLE_" + role.name()));
        // ... et le nom brut du rôle (compatibilité avec l'ancien hasAuthority('ADMIN_PME')).
        authorities.add(new SimpleGrantedAuthority(role.name()));
        // ... ainsi que chaque permission fine, pour hasAuthority('PRODUCT_WRITE'), etc.
        role.getPermissions().forEach(p -> authorities.add(new SimpleGrantedAuthority(p.name())));
        return authorities;
    }

    @Transient
    public String getFullName() {
        return firstName + " " + lastName;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        // Un utilisateur d'une entreprise suspendue ne peut plus se connecter.
        // Le Super-Administrateur (sans entreprise) reste toujours actif.
        return company == null || company.getStatus() == CompanyStatus.ACTIVE;
    }
}