package com.afristock.service;

import com.afristock.model.entity.*;
import com.afristock.model.enums.MovementType;
import com.afristock.model.enums.PurchaseStatus;
import com.afristock.repository.ProductRepository;
import com.afristock.repository.PurchaseOrderRepository;
import com.afristock.repository.SiteRepository;
import com.afristock.repository.SupplierRepository;
import com.afristock.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Achats (Phase 5) : commandes fournisseurs, réception (crédit du stock), paiements et dettes.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class PurchaseService {

    private final PurchaseOrderRepository purchaseRepository;
    private final SupplierRepository supplierRepository;
    private final SiteRepository siteRepository;
    private final ProductRepository productRepository;
    private final StockService stockService;

    @Transactional(readOnly = true)
    public List<PurchaseOrder> getAll() {
        return purchaseRepository.findAllForTenant(TenantContext.getCurrentTenant());
    }

    @Transactional(readOnly = true)
    public PurchaseOrder getWithItems(Long id) {
        return purchaseRepository.findByIdWithItems(id, TenantContext.getCurrentTenant())
                .orElseThrow(() -> new IllegalArgumentException("Commande introuvable."));
    }

    @Transactional(readOnly = true)
    public double supplierDebt(Long supplierId) {
        return purchaseRepository.totalDebtForSupplier(supplierId, TenantContext.getCurrentTenant());
    }

    public PurchaseOrder createOrder(Long supplierId, Long siteId,
                                     List<Long> productIds, List<Integer> quantities, List<Double> unitCosts) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (productIds == null || productIds.isEmpty() || quantities == null
                || unitCosts == null || quantities.size() != productIds.size() || unitCosts.size() != productIds.size()) {
            throw new IllegalArgumentException("Lignes de commande invalides.");
        }

        Supplier supplier = ownedSupplier(supplierId, tenantId);
        Site site = ownedSite(siteId, tenantId);

        PurchaseOrder order = new PurchaseOrder();
        order.setReference(generateReference(tenantId));
        order.setSupplier(supplier);
        order.setSite(site);
        order.setStatus(PurchaseStatus.BROUILLON);
        order.setOrderDate(LocalDateTime.now());
        order.setUser(currentUser());
        order.setTenantId(tenantId);

        double total = 0.0;
        for (int i = 0; i < productIds.size(); i++) {
            Long productId = productIds.get(i);
            Integer qty = quantities.get(i);
            Double cost = unitCosts.get(i);
            if (productId == null || qty == null || qty <= 0) {
                continue;
            }
            Product product = ownedProduct(productId, tenantId);
            double unitCost = cost != null ? cost : (product.getPurchasePrice() != null ? product.getPurchasePrice() : 0.0);
            double lineTotal = unitCost * qty;

            PurchaseOrderItem item = new PurchaseOrderItem();
            item.setProduct(product);
            item.setQuantity(qty);
            item.setUnitCost(unitCost);
            item.setLineTotal(lineTotal);
            order.addItem(item);
            total += lineTotal;
        }

        if (order.getItems().isEmpty()) {
            throw new IllegalArgumentException("La commande doit contenir au moins une ligne valide.");
        }
        order.setTotalAmount(total);
        return purchaseRepository.save(order);
    }

    /** Réceptionne la commande : crédite le stock du site et passe au statut RECUE. */
    public void receive(Long orderId) {
        PurchaseOrder order = getWithItems(orderId);
        if (order.getStatus() == PurchaseStatus.RECUE) {
            throw new IllegalStateException("Commande déjà réceptionnée.");
        }
        if (order.getStatus() == PurchaseStatus.ANNULEE) {
            throw new IllegalStateException("Commande annulée.");
        }
        for (PurchaseOrderItem item : order.getItems()) {
            stockService.recordMovement(item.getProduct().getId(), order.getSite().getId(),
                    item.getQuantity(), MovementType.ENTRY, "Réception " + order.getReference());
        }
        order.setStatus(PurchaseStatus.RECUE);
        order.setReceivedDate(LocalDateTime.now());
        purchaseRepository.save(order);
    }

    public void cancel(Long orderId) {
        PurchaseOrder order = getWithItems(orderId);
        if (order.getStatus() == PurchaseStatus.RECUE) {
            throw new IllegalStateException("Impossible d'annuler une commande déjà réceptionnée.");
        }
        order.setStatus(PurchaseStatus.ANNULEE);
        purchaseRepository.save(order);
    }

    /** Enregistre un paiement (partiel ou total) au fournisseur. */
    public void pay(Long orderId, Double amount) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Le montant du paiement doit être positif.");
        }
        PurchaseOrder order = getWithItems(orderId);
        double newPaid = order.getAmountPaid() + amount;
        if (newPaid > order.getTotalAmount()) {
            newPaid = order.getTotalAmount();
        }
        order.setAmountPaid(newPaid);
        purchaseRepository.save(order);
    }

    // --- helpers ---

    private String generateReference(Long tenantId) {
        long next = purchaseRepository.countByTenantId(tenantId) + 1;
        return String.format("CMD-%06d", next);
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User user) {
            return user;
        }
        return null;
    }

    private Supplier ownedSupplier(Long id, Long tenantId) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Fournisseur introuvable."));
        if (!supplier.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Fournisseur introuvable.");
        }
        return supplier;
    }

    private Site ownedSite(Long id, Long tenantId) {
        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Site introuvable."));
        if (!site.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Site introuvable.");
        }
        return site;
    }

    private Product ownedProduct(Long id, Long tenantId) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Produit introuvable."));
        if (!product.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Produit introuvable.");
        }
        return product;
    }
}
