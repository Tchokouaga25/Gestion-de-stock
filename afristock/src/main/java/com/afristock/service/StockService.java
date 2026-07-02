package com.afristock.service;

import com.afristock.model.entity.Product;
import com.afristock.model.entity.StockLevel;
import com.afristock.model.entity.StockMovement;
import com.afristock.model.enums.MovementType;
import com.afristock.repository.StockLevelRepository;
import com.afristock.repository.StockMovementRepository;
import com.afristock.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Mouvements de stock (entrée / sortie / ajustement) rattachés à un site.
 *
 * <p>Depuis l'unification Phase 3, un mouvement agit sur le {@link StockLevel} du couple
 * (produit, site) puis recalcule le stock total du produit (tous sites).</p>
 */
@Service
@RequiredArgsConstructor
@Transactional
public class StockService {

    private final StockMovementRepository movementRepository;
    private final StockLevelRepository stockLevelRepository;
    private final StockLevelService stockLevelService;

    public void recordMovement(Long productId, Long siteId, Integer quantity, MovementType type, String reason) {
        if (siteId == null) {
            throw new IllegalArgumentException("Le site est obligatoire.");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("La quantité doit être supérieure à zéro.");
        }

        Long tenantId = TenantContext.getCurrentTenant();
        StockLevel level = stockLevelService.getOrCreate(productId, siteId, tenantId);

        switch (type) {
            case ENTRY -> level.setQuantity(level.getQuantity() + quantity);
            case EXIT -> {
                if (level.getQuantity() < quantity) {
                    throw new IllegalStateException("Stock insuffisant sur ce site !");
                }
                level.setQuantity(level.getQuantity() - quantity);
            }
            // Ajustement d'inventaire : la quantité saisie devient la quantité réelle du site.
            case ADJUSTMENT -> level.setQuantity(quantity);
        }
        stockLevelRepository.save(level);

        Product product = level.getProduct();
        StockMovement movement = new StockMovement();
        movement.setProduct(product);
        movement.setSite(level.getSite());
        movement.setQuantity(quantity);
        movement.setType(type);
        movement.setReason(reason);
        movement.setTenantId(tenantId);
        movementRepository.save(movement);

        // Le stock total du produit (tous sites) est dérivé de la somme des StockLevel.
        stockLevelService.recomputeProductTotal(product, tenantId);
    }
}
