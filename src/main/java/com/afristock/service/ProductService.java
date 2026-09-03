package com.afristock.service;

import com.afristock.model.entity.Category;
import com.afristock.model.entity.Product;
import com.afristock.repository.CategoryRepository;
import com.afristock.repository.ProductRepository;
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

    // --- GESTION DES CATÉGORIES ---

    public List<Category> getAllCategories() {
        Long tenantId = TenantContext.getCurrentTenant();
        return categoryRepository.findByTenantId(tenantId);
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
        // Vérifier si des produits sont liés à cette catégorie
        List<Product> products = productRepository.findByCategoryIdAndTenantId(id, tenantId);
        if (!products.isEmpty()) {
            throw new IllegalStateException("Impossible de supprimer une catégorie liée à des produits.");
        }
        categoryRepository.deleteById(id);
    }

    // --- GESTION DES PRODUITS ---

    public List<Product> getAllProducts() {
        Long tenantId = TenantContext.getCurrentTenant();
        return productRepository.findByTenantId(tenantId);
    }

    public Product saveProduct(Product product) {
        Long tenantId = TenantContext.getCurrentTenant();

        if (product.getId() == null && productRepository.existsByReferenceAndTenantId(product.getReference(), tenantId)) {
            throw new IllegalArgumentException("Un produit avec cette référence existe déjà.");
        }

        product.setTenantId(tenantId);
        return productRepository.save(product);
    }

    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produit non trouvé."));

        // Règle métier : Ne pas supprimer si stock > 0
        if (product.getCurrentQuantity() != null && product.getCurrentQuantity() > 0) {
            throw new IllegalStateException("Impossible de supprimer un produit dont le stock n'est pas à zéro.");
        }

        productRepository.deleteById(id);
    }

    public List<Product> searchProducts(String query) {
        Long tenantId = TenantContext.getCurrentTenant();
        return productRepository.findByNameContainingIgnoreCaseAndTenantId(query, tenantId);
    }
}
