package com.afristock.service;

import com.afristock.dto.RegisterRequest;
import com.afristock.dto.RegisterResponse;
import com.afristock.model.entity.Company;
import com.afristock.model.entity.User;
import com.afristock.model.enums.Role;
import com.afristock.repository.CompanyRepository;
import com.afristock.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RegistrationService {

    private final CompanyRepository companyRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegisterResponse registerCompanyAndAdmin(RegisterRequest request) {

        log.info("Tentative d'inscription pour l'email utilisateur : {}", request.getEmail());

        // 1. Vérifications de base
        if (!request.getPassword().equals(request.getPasswordConfirm())) {
            log.warn("Échec inscription : les mots de passe ne correspondent pas pour {}", request.getEmail());
            throw new IllegalArgumentException("Les mots de passe ne correspondent pas");
        }

        // 2. Vérifier que l'email de l'utilisateur n'existe pas déjà
        if (userRepository.existsByEmail(request.getEmail())) {
            log.warn("Échec inscription : l'email utilisateur {} est déjà utilisé", request.getEmail());
            throw new IllegalArgumentException("Cet email est déjà utilisé");
        }

        // 3. Vérifier que l'email de l'entreprise n'existe pas déjà (évite l'erreur SQL Duplicate Key)
        if (companyRepository.existsByEmail(request.getCompanyEmail())) {
            log.warn("Échec inscription : l'email entreprise {} est déjà utilisé", request.getCompanyEmail());
            throw new IllegalArgumentException("Cet email d'entreprise est déjà utilisé");
        }

        // 4. Créer la Company
        Company company = new Company();
        company.setName(request.getCompanyName());
        company.setCity(request.getCompanyCity());
        company.setPhone(request.getCompanyPhone());
        company.setEmail(request.getCompanyEmail());
        company.setCreatedAt(LocalDateTime.now());

        Company savedCompany = companyRepository.saveAndFlush(company);
        log.info("Compagnie enregistrée avec succès. ID: {}, Nom: {}", savedCompany.getId(), savedCompany.getName());

        // 5. Créer l'utilisateur ADMIN_PME
        User admin = new User();
        admin.setCompany(savedCompany);
        admin.setEmail(request.getEmail());
        admin.setFirstName(request.getFirstName());
        admin.setLastName(request.getLastName());
        admin.setPassword(passwordEncoder.encode(request.getPassword()));
        admin.setRole(Role.ADMIN_PME);

        User savedUser = userRepository.saveAndFlush(admin);
        log.info("Utilisateur administrateur créé avec succès. ID: {}, Email: {}", savedUser.getId(), savedUser.getEmail());

        // 6. Réponse
        return new RegisterResponse(
                "Entreprise et compte administrateur créés avec succès",
                savedCompany.getId(),
                admin.getEmail()
        );
    }
}
