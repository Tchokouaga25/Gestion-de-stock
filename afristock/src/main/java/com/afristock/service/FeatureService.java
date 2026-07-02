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
}
