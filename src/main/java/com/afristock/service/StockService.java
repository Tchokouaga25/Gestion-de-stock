package com.afristock.service;

import com.afristock.model.entity.Product;
import com.afristock.model.entity.StockMovement;
import com.afristock.model.enums.MovementType;
import com.afristock.repository.ProductRepository;
import com.afristock.repository.StockMovementRepository;
import com.afristock.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class StockService {

    private final ProductRepository productRepository;
    private final StockMovementRepository movementRepository;

    public void recordMovement(Long productId, Integer quantity, MovementType type, String reason) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Produit non trouvé"));

        // Créer le mouvement
        StockMovement movement = new StockMovement();
        movement.setProduct(product);
        movement.setQuantity(quantity);
        movement.setType(type);
        movement.setReason(reason);
        movement.setTenantId(TenantContext.getCurrentTenant());

        // Mettre à jour la quantité du produit
        if (type == MovementType.ENTRY) {
            product.setCurrentQuantity((product.getCurrentQuantity() == null ? 0 : product.getCurrentQuantity()) + quantity);
        } else if (type == MovementType.EXIT) {
            int current = (product.getCurrentQuantity() == null ? 0 : product.getCurrentQuantity());
            if (current < quantity) {
                throw new IllegalStateException("Stock insuffisant !");
            }
            product.setCurrentQuantity(current - quantity);
        }

        productRepository.save(product);
        movementRepository.save(movement);
    }
}
