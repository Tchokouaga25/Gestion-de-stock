package com.afristock.service;

import com.afristock.model.entity.Product;
import com.afristock.model.entity.Site;
import com.afristock.model.entity.StockLevel;
import com.afristock.repository.ProductRepository;
import com.afristock.repository.SiteRepository;
import com.afristock.repository.StockLevelRepository;
import com.afristock.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Gestion du stock par site (niveaux de stock).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class StockLevelService {

    private final StockLevelRepository stockLevelRepository;
    private final ProductRepository productRepository;
    private final SiteRepository siteRepository;

    @Transactional(readOnly = true)
    public List<StockLevel> getAll() {
        return stockLevelRepository.findAllForTenant(TenantContext.getCurrentTenant());
    }

    /** Stocks en rupture ou sous le seuil d'alerte, par site. */
    @Transactional(readOnly = true)
    public List<StockLevel> getLowStock() {
        return stockLevelRepository.findLowStock(TenantContext.getCurrentTenant());
    }

    /** Valorisation totale du stock (quantité x prix d'achat), tous sites confondus. */
    @Transactional(readOnly = true)
    public double getTotalStockValue() {
        return stockLevelRepository.sumStockValue(TenantContext.getCurrentTenant());
    }

    @Transactional(readOnly = true)
    public List<StockLevel> getBySite(Long siteId) {
        return stockLevelRepository.findBySiteIdAndTenantId(siteId, TenantContext.getCurrentTenant());
    }

    /**
     * Définit (écrase) la quantité d'un produit dans un site. Crée la ligne de stock si absente.
     */
    public void setQuantity(Long productId, Long siteId, Integer quantity) {
        if (quantity == null || quantity < 0) {
            throw new IllegalArgumentException("La quantité doit être positive.");
        }
        Long tenantId = TenantContext.getCurrentTenant();
        StockLevel level = getOrCreate(productId, siteId, tenantId);
        level.setQuantity(quantity);
        stockLevelRepository.save(level);
        recomputeProductTotal(level.getProduct(), tenantId);
    }

    /**
     * Recalcule et met à jour le stock total d'un produit (tous sites confondus).
     * {@code Product.currentQuantity} est une valeur dérivée = somme des {@link StockLevel}.
     */
    public void recomputeProductTotal(Product product, Long tenantId) {
        int total = stockLevelRepository.sumQuantityForProduct(product.getId(), tenantId);
        product.setCurrentQuantity(total);
        productRepository.save(product);
    }

    /**
     * Charge la ligne de stock (produit, site) en la créant si nécessaire. Vérifie l'appartenance
     * du produit et du site à l'entreprise courante.
     */
    StockLevel getOrCreate(Long productId, Long siteId, Long tenantId) {
        return stockLevelRepository.findByProductIdAndSiteIdAndTenantId(productId, siteId, tenantId)
                .orElseGet(() -> {
                    Product product = ownedProduct(productId, tenantId);
                    Site site = ownedSite(siteId, tenantId);
                    StockLevel level = new StockLevel();
                    level.setProduct(product);
                    level.setSite(site);
                    level.setQuantity(0);
                    level.setTenantId(tenantId);
                    return level;
                });
    }

    private Product ownedProduct(Long productId, Long tenantId) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Produit introuvable."));
        if (!product.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Produit introuvable.");
        }
        return product;
    }

    private Site ownedSite(Long siteId, Long tenantId) {
        Site site = siteRepository.findById(siteId)
                .orElseThrow(() -> new IllegalArgumentException("Site introuvable."));
        if (!site.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Site introuvable.");
        }
        return site;
    }
}
