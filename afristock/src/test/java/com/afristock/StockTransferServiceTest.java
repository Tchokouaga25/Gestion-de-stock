package com.afristock;

import com.afristock.model.entity.Category;
import com.afristock.model.entity.Company;
import com.afristock.model.entity.Product;
import com.afristock.model.entity.Site;
import com.afristock.model.enums.CompanyStatus;
import com.afristock.model.enums.SiteType;
import com.afristock.repository.*;
import com.afristock.security.TenantContext;
import com.afristock.service.StockLevelService;
import com.afristock.service.StockTransferService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Valide la logique de transfert de stock entre sites (Phase 3).
 */
@SpringBootTest
@Transactional
class StockTransferServiceTest {

    @Autowired private StockLevelService stockLevelService;
    @Autowired private StockTransferService stockTransferService;
    @Autowired private CompanyRepository companyRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private SiteRepository siteRepository;
    @Autowired private StockLevelRepository stockLevelRepository;

    @AfterEach
    void clearTenant() {
        TenantContext.clear();
    }

    @Test
    void transfertDebiteLaSourceEtCrediteLaDestination() {
        Long tenantId = setupTenant();
        Category cat = saveCategory("Boissons", tenantId);
        Product product = saveProduct("REF-1", "Jus d'orange", cat, tenantId);
        Site entrepot = saveSite("Entrepôt", SiteType.ENTREPOT, tenantId);
        Site boutique = saveSite("Boutique Centre", SiteType.BOUTIQUE, tenantId);

        stockLevelService.setQuantity(product.getId(), entrepot.getId(), 10);

        stockTransferService.transfer(product.getId(), entrepot.getId(), boutique.getId(), 4, "Réappro");

        assertThat(quantity(product.getId(), entrepot.getId(), tenantId)).isEqualTo(6);
        assertThat(quantity(product.getId(), boutique.getId(), tenantId)).isEqualTo(4);
    }

    @Test
    void transfertRefuseSiStockInsuffisant() {
        Long tenantId = setupTenant();
        Category cat = saveCategory("Boissons", tenantId);
        Product product = saveProduct("REF-2", "Eau minérale", cat, tenantId);
        Site entrepot = saveSite("Entrepôt", SiteType.ENTREPOT, tenantId);
        Site boutique = saveSite("Boutique", SiteType.BOUTIQUE, tenantId);

        stockLevelService.setQuantity(product.getId(), entrepot.getId(), 3);

        assertThatThrownBy(() ->
                stockTransferService.transfer(product.getId(), entrepot.getId(), boutique.getId(), 5, null))
                .isInstanceOf(IllegalStateException.class);
    }

    // --- helpers ---

    private Long setupTenant() {
        Company c = new Company();
        c.setName("Test SARL " + System.nanoTime());
        c.setEmail("test" + System.nanoTime() + "@x.com");
        c.setStatus(CompanyStatus.ACTIVE);
        c = companyRepository.save(c);
        TenantContext.setCurrentTenant(c.getId());
        return c.getId();
    }

    private Category saveCategory(String name, Long tenantId) {
        Category cat = new Category();
        cat.setName(name);
        cat.setTenantId(tenantId);
        return categoryRepository.save(cat);
    }

    private Product saveProduct(String ref, String name, Category cat, Long tenantId) {
        Product p = new Product();
        p.setReference(ref);
        p.setName(name);
        p.setCategory(cat);
        p.setTenantId(tenantId);
        return productRepository.save(p);
    }

    private Site saveSite(String name, SiteType type, Long tenantId) {
        Site s = new Site();
        s.setName(name);
        s.setType(type);
        s.setTenantId(tenantId);
        return siteRepository.save(s);
    }

    private int quantity(Long productId, Long siteId, Long tenantId) {
        return stockLevelRepository.findByProductIdAndSiteIdAndTenantId(productId, siteId, tenantId)
                .orElseThrow().getQuantity();
    }
}
