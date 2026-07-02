package com.afristock.config;

import com.afristock.model.entity.Feature;
import com.afristock.model.entity.User;
import com.afristock.model.enums.Role;
import com.afristock.repository.FeatureRepository;
import com.afristock.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.ApplicationArguments;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Initialise les données indispensables au démarrage :
 *   * le compte Super-Administrateur (si absent),
 *   * le catalogue des fonctionnalités (feature flags).
 *
 * <p>Idempotent : ne crée que ce qui manque.</p>
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements ApplicationRunner {

    private final UserRepository userRepository;
    private final FeatureRepository featureRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${afristock.super-admin.email:admin@afristock.com}")
    private String superAdminEmail;

    @Value("${afristock.super-admin.password:ChangeMe123!}")
    private String superAdminPassword;

    @Override
    public void run(ApplicationArguments args) {
        initSuperAdmin();
        initFeatures();
    }

    private void initSuperAdmin() {
        if (userRepository.existsByEmail(superAdminEmail)) {
            return;
        }
        User admin = new User();
        admin.setEmail(superAdminEmail);
        admin.setPassword(passwordEncoder.encode(superAdminPassword));
        admin.setFirstName("Super");
        admin.setLastName("Admin");
        admin.setRole(Role.SUPER_ADMIN);
        admin.setCompany(null); // pas d'entreprise : c'est l'exploitant de la plateforme
        userRepository.save(admin);
        log.info("Compte Super-Administrateur créé : {}", superAdminEmail);
    }

    private void initFeatures() {
        // code, nom, catégorie, description
        List<String[]> defaults = List.of(
                new String[]{"SITES",     "Boutiques & entrepôts","Référentiel", "Gestion des sites (boutiques, entrepôt principal)"},
                new String[]{"SUPPLIERS", "Fournisseurs",         "Référentiel", "Fiches fournisseurs et coordonnées"},
                new String[]{"CUSTOMERS", "Clients",              "Référentiel", "Fiches clients, fidélité et plafond de crédit"},
                new String[]{"PRODUCTS",  "Gestion des produits", "Stock",   "Catalogue, catégories et fiches produits"},
                new String[]{"STOCK",     "Gestion du stock",     "Stock",   "Mouvements, entrées/sorties, ajustements"},
                new String[]{"SALES",     "Ventes & caisse",      "Ventes",  "Création de ventes, factures et encaissement"},
                new String[]{"PURCHASES", "Achats",               "Achats",  "Commandes fournisseurs et réceptions"},
                new String[]{"REPORTS",   "Rapports",             "Pilotage", "Rapports de fin de journée, PDF/Excel"},
                new String[]{"HR",        "Ressources humaines",  "RH",      "Employés, contrats, congés, pointage"}
        );
        for (String[] f : defaults) {
            if (!featureRepository.existsByCode(f[0])) {
                Feature feature = new Feature();
                feature.setCode(f[0]);
                feature.setName(f[1]);
                feature.setCategory(f[2]);
                feature.setDescription(f[3]);
                feature.setEnabled(true);
                featureRepository.save(feature);
            }
        }
    }
}
