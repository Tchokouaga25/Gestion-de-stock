package com.afristock.service;

import com.afristock.model.entity.Brand;
import com.afristock.model.entity.Category;
import com.afristock.model.entity.Product;
import com.afristock.model.entity.ProductVariant;
import com.afristock.repository.BrandRepository;
import com.afristock.repository.CategoryRepository;
import com.afristock.repository.ProductRepository;
import com.afristock.repository.ProductVariantRepository;
import com.afristock.security.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final BrandRepository brandRepository;
    private final ProductVariantRepository variantRepository;

    // --- GESTION DES CATÉGORIES ---

    @Transactional(readOnly = true)
    public List<Category> getAllCategories() {
        return categoryRepository.findByTenantId(TenantContext.getCurrentTenant());
    }

    public Category saveCategory(Category category) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (categoryRepository.existsByNameAndTenantId(category.getName(), tenantId)) {
            throw new IllegalArgumentException("Une catégorie avec ce nom existe déjà.");
        }
        category.setTenantId(tenantId);
        return categoryRepository.save(category);
    }

    public void deleteCategory(Long id) {
        Long tenantId = TenantContext.getCurrentTenant();
        List<Product> products = productRepository.findByCategoryIdAndTenantId(id, tenantId);
        if (!products.isEmpty()) {
            throw new IllegalStateException("Impossible de supprimer une catégorie liée à des produits.");
        }
        categoryRepository.deleteById(id);
    }

    // --- GESTION DES MARQUES ---

    @Transactional(readOnly = true)
    public List<Brand> getAllBrands() {
        return brandRepository.findByTenantIdOrderByName(TenantContext.getCurrentTenant());
    }

    public Brand saveBrand(Brand brand) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (brand.getId() == null && brandRepository.existsByNameAndTenantId(brand.getName(), tenantId)) {
            throw new IllegalArgumentException("Une marque avec ce nom existe déjà.");
        }
        brand.setTenantId(tenantId);
        return brandRepository.save(brand);
    }

    public void deleteBrand(Long id) {
        brandRepository.deleteById(id);
    }

    // --- GESTION DES PRODUITS ---

    @Transactional(readOnly = true)
    public List<Product> getAllProducts() {
        return productRepository.findByTenantId(TenantContext.getCurrentTenant());
    }

    /** Produits actifs uniquement : utilisé par les sélecteurs de nouvelle vente/nouvel achat. */
    @Transactional(readOnly = true)
    public List<Product> getActiveProducts() {
        return productRepository.findByTenantIdAndActive(TenantContext.getCurrentTenant(), true);
    }

    /** Liste des produits pour l'écran catalogue : recherche optionnelle croisée avec un filtre de statut optionnel. */
    @Transactional(readOnly = true)
    public List<Product> getProducts(String search, Boolean active) {
        Long tenantId = TenantContext.getCurrentTenant();
        boolean hasSearch = search != null && !search.isBlank();
        if (active == null) {
            return hasSearch
                    ? productRepository.findByNameContainingIgnoreCaseAndTenantId(search, tenantId)
                    : productRepository.findByTenantId(tenantId);
        }
        return hasSearch
                ? productRepository.findByNameContainingIgnoreCaseAndTenantIdAndActive(search, tenantId, active)
                : productRepository.findByTenantIdAndActive(tenantId, active);
    }

    @Transactional(readOnly = true)
    public Product getProduct(Long id) {
        return loadOwnedProduct(id);
    }

    public Product saveProduct(Product product, Long categoryId, Long brandId) {
        Long tenantId = TenantContext.getCurrentTenant();

        if (product.getId() == null && productRepository.existsByReferenceAndTenantId(product.getReference(), tenantId)) {
            throw new IllegalArgumentException("Un produit avec cette référence existe déjà.");
        }

        // Résolution des associations par identifiant (les selects du formulaire renvoient des ids).
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new IllegalArgumentException("Catégorie invalide."));
        if (!category.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Catégorie invalide.");
        }
        product.setCategory(category);

        if (brandId != null) {
            Brand brand = brandRepository.findById(brandId)
                    .orElseThrow(() -> new IllegalArgumentException("Marque invalide."));
            if (!brand.getTenantId().equals(tenantId)) {
                throw new IllegalArgumentException("Marque invalide.");
            }
            product.setBrand(brand);
        } else {
            product.setBrand(null);
        }

        product.setTenantId(tenantId);
        return productRepository.save(product);
    }

    public Product toggleActive(Long id) {
        Product product = loadOwnedProduct(id);
        product.setActive(!product.isActive());
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        Product product = loadOwnedProduct(id);
        if (product.getCurrentQuantity() != null && product.getCurrentQuantity() > 0) {
            throw new IllegalStateException("Impossible de supprimer un produit dont le stock n'est pas à zéro.");
        }
        productRepository.delete(product);
    }

    // --- GESTION DES VARIANTES ---

    @Transactional(readOnly = true)
    public List<ProductVariant> getVariants(Long productId) {
        return variantRepository.findByProductIdAndTenantId(productId, TenantContext.getCurrentTenant());
    }

    public void addVariant(Long productId, ProductVariant variant) {
        Product product = loadOwnedProduct(productId);
        variant.setProduct(product);
        variant.setTenantId(TenantContext.getCurrentTenant());
        variantRepository.save(variant);
    }

    public void deleteVariant(Long variantId) {
        ProductVariant variant = variantRepository.findById(variantId)
                .orElseThrow(() -> new IllegalArgumentException("Variante introuvable."));
        if (!variant.getTenantId().equals(TenantContext.getCurrentTenant())) {
            throw new IllegalArgumentException("Variante introuvable.");
        }
        variantRepository.delete(variant);
    }

    // --- Helper sécurité multi-tenant pour les accès par id ---

    private Product loadOwnedProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produit non trouvé."));
        if (!product.getTenantId().equals(TenantContext.getCurrentTenant())) {
            throw new IllegalArgumentException("Produit non trouvé.");
        }
        return product;
    }
}
