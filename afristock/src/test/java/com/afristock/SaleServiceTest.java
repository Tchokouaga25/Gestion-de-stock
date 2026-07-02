package com.afristock;

import com.afristock.model.entity.*;
import com.afristock.model.enums.CompanyStatus;
import com.afristock.model.enums.PaymentMethod;
import com.afristock.model.enums.SaleType;
import com.afristock.model.enums.SiteType;
import com.afristock.repository.*;
import com.afristock.security.TenantContext;
import com.afristock.service.SaleService;
import com.afristock.service.StockLevelService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Valide la création d'une vente (Phase 4) : totaux, numérotation, décrément du stock du site.
 */
@SpringBootTest
@Transactional
class SaleServiceTest {

    @Autowired private SaleService saleService;
    @Autowired private StockLevelService stockLevelService;
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
    void venteDecrementeLeStockDuSiteEtGenereUneFacture() {
        Long tenantId = setupTenant();
        Product product = product("Jus", 1000.0, tenantId);
        Site boutique = site("Boutique", tenantId);
        stockLevelService.setQuantity(product.getId(), boutique.getId(), 10);

        Sale sale = saleService.createSale(boutique.getId(), null, SaleType.DETAIL, PaymentMethod.ESPECES,
                2000.0, List.of(product.getId()), List.of(2));

        assertThat(sale.getReference()).startsWith("FAC-");
        assertThat(sale.getTotalAmount()).isEqualTo(2000.0);
        assertThat(sale.getItems()).hasSize(1);
        assertThat(quantity(product.getId(), boutique.getId(), tenantId)).isEqualTo(8);
    }

    @Test
    void venteRefuseeSiStockInsuffisant() {
        Long tenantId = setupTenant();
        Product product = product("Eau", 500.0, tenantId);
        Site boutique = site("Boutique", tenantId);
        stockLevelService.setQuantity(product.getId(), boutique.getId(), 1);

        assertThatThrownBy(() -> saleService.createSale(boutique.getId(), null, SaleType.DETAIL,
                PaymentMethod.ESPECES, 2500.0, List.of(product.getId()), List.of(5)))
                .isInstanceOf(RuntimeException.class);
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

    private Product product(String name, Double price, Long tenantId) {
        Category cat = new Category();
        cat.setName("Cat " + System.nanoTime());
        cat.setTenantId(tenantId);
        cat = categoryRepository.save(cat);

        Product p = new Product();
        p.setReference("REF-" + System.nanoTime());
        p.setName(name);
        p.setSalePrice(price);
        p.setCategory(cat);
        p.setTenantId(tenantId);
        return productRepository.save(p);
    }

    private Site site(String name, Long tenantId) {
        Site s = new Site();
        s.setName(name);
        s.setType(SiteType.BOUTIQUE);
        s.setTenantId(tenantId);
        return siteRepository.save(s);
    }

    private int quantity(Long productId, Long siteId, Long tenantId) {
        return stockLevelRepository.findByProductIdAndSiteIdAndTenantId(productId, siteId, tenantId)
                .orElseThrow().getQuantity();
    }
}
