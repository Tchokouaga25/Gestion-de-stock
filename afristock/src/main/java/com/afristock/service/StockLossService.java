package com.afristock.service;

import com.afristock.model.entity.StockLevel;
import com.afristock.model.entity.StockLoss;
import com.afristock.model.entity.User;
import com.afristock.model.enums.LossType;
import com.afristock.model.enums.MovementType;
import com.afristock.repository.StockLevelRepository;
import com.afristock.repository.StockLossRepository;
import com.afristock.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Déclaration des pertes (produits périmés / avariés).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class StockLossService {

    private final StockLossRepository stockLossRepository;
    private final StockLevelRepository stockLevelRepository;
    private final StockService stockService;

    @Transactional(readOnly = true)
    public List<StockLoss> getAll() {
        return stockLossRepository.findAllForTenant(TenantContext.getCurrentTenant());
    }

    public void declareLoss(Long productId, Long siteId, Integer quantity, LossType type, String reason) {
        Long tenantId = TenantContext.getCurrentTenant();

        // Décrément du stock du site (contrôle de suffisance inclus) + mouvement de sortie.
        stockService.recordMovement(productId, siteId, quantity, MovementType.EXIT,
                "Perte (" + type + ")" + (reason != null && !reason.isBlank() ? " : " + reason : ""));

        // Trace catégorisée pour les rapports.
        StockLevel level = stockLevelRepository
                .findByProductIdAndSiteIdAndTenantId(productId, siteId, tenantId)
                .orElseThrow(() -> new IllegalStateException("Stock introuvable."));

        StockLoss loss = new StockLoss();
        loss.setProduct(level.getProduct());
        loss.setSite(level.getSite());
        loss.setType(type);
        loss.setQuantity(quantity);
        loss.setReason(reason);
        loss.setUser(currentUser());
        loss.setTenantId(tenantId);
        stockLossRepository.save(loss);
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User user) {
            return user;
        }
        return null;
    }
}
