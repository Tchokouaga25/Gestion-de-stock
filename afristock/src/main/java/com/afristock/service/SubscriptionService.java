package com.afristock.service;

import com.afristock.model.entity.Subscription;
import com.afristock.model.entity.SubscriptionPlan;
import com.afristock.model.enums.SubscriptionStatus;
import com.afristock.repository.SubscriptionPlanRepository;
import com.afristock.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Abonnements SaaS (Phase 9) : offres, essai gratuit, souscription, expiration.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SubscriptionService {

    private static final int TRIAL_DAYS = 14;

    private final SubscriptionRepository subscriptionRepository;
    private final SubscriptionPlanRepository planRepository;

    // --- Offres (Super-Admin) ---

    @Transactional(readOnly = true)
    public List<SubscriptionPlan> getPlans() {
        return planRepository.findAllByOrderByMonthlyPriceAsc();
    }

    public SubscriptionPlan savePlan(SubscriptionPlan plan) {
        return planRepository.save(plan);
    }

    // --- Abonnement d'une entreprise ---

    @Transactional(readOnly = true)
    public Optional<Subscription> forCompany(Long companyId) {
        return subscriptionRepository.findByCompanyId(companyId);
    }

    /** Démarre une période d'essai (à l'inscription d'une entreprise). */
    public void startTrial(Long companyId) {
        if (subscriptionRepository.findByCompanyId(companyId).isPresent()) {
            return;
        }
        Subscription sub = new Subscription();
        sub.setCompanyId(companyId);
        sub.setPlanName("Essai gratuit");
        sub.setStatus(SubscriptionStatus.ESSAI);
        sub.setStartDate(LocalDate.now());
        sub.setEndDate(LocalDate.now().plusDays(TRIAL_DAYS));
        subscriptionRepository.save(sub);
    }

    /** Souscrit / renouvelle une entreprise à une offre pour un nombre de mois donné. */
    public void subscribe(Long companyId, Long planId, int months) {
        if (months <= 0) {
            throw new IllegalArgumentException("La durée doit être d'au moins un mois.");
        }
        SubscriptionPlan plan = planRepository.findById(planId)
                .orElseThrow(() -> new IllegalArgumentException("Offre introuvable."));

        Subscription sub = subscriptionRepository.findByCompanyId(companyId)
                .orElseGet(() -> {
                    Subscription s = new Subscription();
                    s.setCompanyId(companyId);
                    s.setStartDate(LocalDate.now());
                    return s;
                });
        // Renouvellement : on prolonge depuis la date de fin si encore valide, sinon depuis aujourd'hui.
        LocalDate base = (sub.getEndDate() != null && sub.getEndDate().isAfter(LocalDate.now()))
                ? sub.getEndDate() : LocalDate.now();
        sub.setPlanName(plan.getName());
        sub.setStatus(SubscriptionStatus.ACTIF);
        sub.setEndDate(base.plusMonths(months));
        subscriptionRepository.save(sub);
    }
}
