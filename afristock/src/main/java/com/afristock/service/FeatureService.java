package com.afristock.service;

import com.afristock.model.entity.Feature;
import com.afristock.repository.FeatureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Gestion des « feature flags » globaux.
 *
 * <p>Exposé aux templates Thymeleaf : on peut écrire
 * {@code th:if="${@featureService.isEnabled('PRODUCTS')}"} pour masquer un menu lorsqu'une
 * fonctionnalité est désactivée par le Super-Administrateur.</p>
 */
@Service("featureService")
@RequiredArgsConstructor
@Transactional
public class FeatureService {

    private final FeatureRepository featureRepository;

    @Transactional(readOnly = true)
    public List<Feature> getAll() {
        return featureRepository.findAllByOrderByCategoryAscNameAsc();
    }

    /**
     * Indique si une fonctionnalité est active. Une fonctionnalité inconnue est considérée comme
     * active (fail-open) afin de ne pas masquer un écran tant qu'aucun flag n'a été défini pour lui.
     */
    @Transactional(readOnly = true)
    public boolean isEnabled(String code) {
        return featureRepository.findByCode(code)
                .map(Feature::isEnabled)
                .orElse(true);
    }

    public void setEnabled(Long id, boolean enabled) {
        Feature feature = featureRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Fonctionnalité introuvable."));
        feature.setEnabled(enabled);
        featureRepository.save(feature);
    }

    /** Crée une nouvelle fonctionnalité. Le code technique doit être unique. */
    public Feature create(String code, String name, String category, String description) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Le code technique est obligatoire.");
        }
        String normalizedCode = code.trim().toUpperCase().replace(' ', '_');
        if (featureRepository.existsByCode(normalizedCode)) {
            throw new IllegalArgumentException("Ce code technique existe déjà.");
        }
        Feature feature = new Feature();
        feature.setCode(normalizedCode);
        feature.setName(name);
        feature.setCategory(category);
        feature.setDescription(description);
        feature.setEnabled(true);
        return featureRepository.save(feature);
    }

    /** Modifie le nom, la catégorie et la description. Le code technique reste immuable après
     *  création : il est référencé tel quel dans les vues ({@code featureService.isEnabled('...')}). */
    public void update(Long id, String name, String category, String description) {
        Feature feature = featureRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Fonctionnalité introuvable."));
        feature.setName(name);
        feature.setCategory(category);
        feature.setDescription(description);
        featureRepository.save(feature);
    }

    public void delete(Long id) {
        if (!featureRepository.existsById(id)) {
            throw new IllegalArgumentException("Fonctionnalité introuvable.");
        }
        featureRepository.deleteById(id);
    }
}
