package com.afristock.service;

import com.afristock.model.entity.*;
import com.afristock.model.enums.MovementType;
import com.afristock.repository.InventorySessionRepository;
import com.afristock.repository.SiteRepository;
import com.afristock.repository.StockLevelRepository;
import com.afristock.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Inventaires physiques par site (Phase 3).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class InventoryService {

    private final InventorySessionRepository sessionRepository;
    private final StockLevelRepository stockLevelRepository;
    private final SiteRepository siteRepository;
    private final StockService stockService;

    @Transactional(readOnly = true)
    public List<InventorySession> getAll() {
        return sessionRepository.findAllForTenant(TenantContext.getCurrentTenant());
    }

    @Transactional(readOnly = true)
    public InventorySession getWithLines(Long id) {
        return sessionRepository.findByIdWithLines(id, TenantContext.getCurrentTenant())
                .orElseThrow(() -> new IllegalArgumentException("Inventaire introuvable."));
    }

    /** Les lignes de stock existantes d'un site, pour saisir les quantités comptées. */
    @Transactional(readOnly = true)
    public List<StockLevel> stockOfSite(Long siteId) {
        return stockLevelRepository.findBySiteIdAndTenantId(siteId, TenantContext.getCurrentTenant());
    }

    /**
     * Valide un inventaire : enregistre la session et applique un ajustement par ligne comptée.
     */
    public InventorySession validate(Long siteId, String note, List<Long> productIds, List<Integer> countedQuantities) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (productIds == null || productIds.isEmpty() || countedQuantities == null
                || countedQuantities.size() != productIds.size()) {
            throw new IllegalArgumentException("Saisie d'inventaire invalide.");
        }

        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new IllegalArgumentException("Site introuvable."));
        if (!site.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Site introuvable.");
        }

        InventorySession session = new InventorySession();
        session.setSite(site);
        session.setNote(note);
        session.setUser(currentUser());
        session.setTenantId(tenantId);

        for (int i = 0; i < productIds.size(); i++) {
            Long productId = productIds.get(i);
            Integer counted = countedQuantities.get(i);
            if (productId == null || counted == null || counted < 0) {
                continue;
            }
            int previous = stockLevelRepository
                    .findByProductIdAndSiteIdAndTenantId(productId, siteId, tenantId)
                    .map(StockLevel::getQuantity)
                    .orElse(0);

            // Applique la quantité comptée comme quantité réelle (ajustement).
            stockService.recordMovement(productId, siteId, counted, MovementType.ADJUSTMENT,
                    "Inventaire");

            InventoryLine line = new InventoryLine();
            line.setProduct(stockLevelRepository
                    .findByProductIdAndSiteIdAndTenantId(productId, siteId, tenantId)
                    .orElseThrow().getProduct());
            line.setPreviousQuantity(previous);
            line.setCountedQuantity(counted);
            session.addLine(line);
        }

        return sessionRepository.save(session);
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User user) {
            return user;
        }
        return null;
    }
}
