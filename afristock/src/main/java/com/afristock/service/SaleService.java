package com.afristock.service;

import com.afristock.model.entity.*;
import com.afristock.model.enums.MovementType;
import com.afristock.model.enums.PaymentMethod;
import com.afristock.model.enums.SaleType;
import com.afristock.repository.CustomerRepository;
import com.afristock.repository.ProductRepository;
import com.afristock.repository.SaleRepository;
import com.afristock.repository.SiteRepository;
import com.afristock.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Ventes & caisse (Phase 4).
 *
 * <p>Une vente : décrémente le stock du site (via des mouvements de sortie), fige les prix,
 * gère le crédit client (plafond) et la fidélité, et numérote la facture.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SaleService {

    /** 1 point de fidélité par tranche de ce montant dépensé. */
    private static final double LOYALTY_POINT_PER = 1000.0;

    private final SaleRepository saleRepository;
    private final SiteRepository siteRepository;
    private final CustomerRepository customerRepository;
    private final ProductRepository productRepository;
    private final StockService stockService;

    @Transactional(readOnly = true)
    public List<Sale> getAll() {
        return saleRepository.findByTenantIdOrderBySaleDateDesc(TenantContext.getCurrentTenant());
    }

    @Transactional(readOnly = true)
    public Page<Sale> getPage(int page, int size) {
        return saleRepository.findByTenantIdOrderBySaleDateDesc(
                TenantContext.getCurrentTenant(), PageRequest.of(Math.max(page, 0), size));
    }

    @Transactional(readOnly = true)
    public Sale getReceipt(Long id) {
        return saleRepository.findByIdWithItems(id, TenantContext.getCurrentTenant())
                .orElseThrow(() -> new IllegalArgumentException("Vente introuvable."));
    }

    public Sale createSale(Long siteId, Long customerId, SaleType saleType, PaymentMethod paymentMethod,
                           Double amountPaid, List<Long> productIds, List<Integer> quantities) {
        Long tenantId = TenantContext.getCurrentTenant();

        if (productIds == null || productIds.isEmpty()) {
            throw new IllegalArgumentException("La vente doit contenir au moins une ligne.");
        }
        if (quantities == null || quantities.size() != productIds.size()) {
            throw new IllegalArgumentException("Lignes de vente invalides.");
        }

        Site site = ownedSite(siteId, tenantId);

        Customer customer = null;
        if (customerId != null) {
            customer = ownedCustomer(customerId, tenantId);
        }

        Sale sale = new Sale();
        sale.setReference(generateReference(tenantId));
        sale.setSite(site);
        sale.setCustomer(customer);
        sale.setUser(currentUser());
        sale.setSaleType(saleType != null ? saleType : SaleType.DETAIL);
        sale.setPaymentMethod(paymentMethod != null ? paymentMethod : PaymentMethod.ESPECES);
        sale.setSaleDate(LocalDateTime.now());
        sale.setTenantId(tenantId);

        double total = 0.0;
        for (int i = 0; i < productIds.size(); i++) {
            Long productId = productIds.get(i);
            Integer qty = quantities.get(i);
            if (productId == null || qty == null || qty <= 0) {
                continue; // ligne vide ignorée
            }
            Product product = ownedProduct(productId, tenantId);
            double unitPrice = resolveUnitPrice(product, sale.getSaleType());
            double lineTotal = unitPrice * qty;

            SaleItem item = new SaleItem();
            item.setProduct(product);
            item.setQuantity(qty);
            item.setUnitPrice(unitPrice);
            item.setLineTotal(lineTotal);
            sale.addItem(item);
            total += lineTotal;
        }

        if (sale.getItems().isEmpty() || total <= 0) {
            throw new IllegalArgumentException("La vente doit contenir au moins une ligne valide.");
        }

        double paid = amountPaid != null ? amountPaid : 0.0;
        sale.setTotalAmount(total);
        sale.setAmountPaid(paid);

        // Règles de paiement.
        if (sale.getPaymentMethod() == PaymentMethod.CREDIT) {
            if (customer == null) {
                throw new IllegalArgumentException("Une vente à crédit nécessite un client.");
            }
            double due = total - paid;
            if (due > customer.getCreditLimit()) {
                throw new IllegalStateException("Plafond de crédit dépassé pour ce client.");
            }
        } else if (paid < total) {
            throw new IllegalArgumentException("Le montant encaissé est insuffisant.");
        }

        // Persistance de la vente (cascade des lignes).
        Sale saved = saleRepository.save(sale);

        // Sortie de stock par site (crée les mouvements et met à jour StockLevel + total produit).
        for (SaleItem item : saved.getItems()) {
            stockService.recordMovement(item.getProduct().getId(), site.getId(),
                    item.getQuantity(), MovementType.EXIT, "Vente " + saved.getReference());
        }

        // Fidélité.
        if (customer != null) {
            int earned = (int) (total / LOYALTY_POINT_PER);
            if (earned > 0) {
                customer.setLoyaltyPoints(customer.getLoyaltyPoints() + earned);
                customerRepository.save(customer);
            }
        }

        return saved;
    }

    /** Récapitulatif de caisse du jour. */
    @Transactional(readOnly = true)
    public CashSummary getTodayCashSummary() {
        Long tenantId = TenantContext.getCurrentTenant();
        LocalDateTime from = LocalDate.now().atStartOfDay();
        LocalDateTime to = from.plusDays(1);
        List<Sale> sales = saleRepository.findForPeriod(tenantId, from, to);

        double total = 0, especes = 0, mobile = 0, carte = 0, credit = 0;
        for (Sale s : sales) {
            total += s.getAmountPaid();
            switch (s.getPaymentMethod()) {
                case ESPECES -> especes += s.getAmountPaid();
                case MOBILE_MONEY -> mobile += s.getAmountPaid();
                case CARTE -> carte += s.getAmountPaid();
                case CREDIT -> credit += s.getBalanceDue();
            }
        }
        return new CashSummary(sales, total, especes, mobile, carte, credit);
    }

    public record CashSummary(List<Sale> sales, double totalEncaisse,
                              double especes, double mobileMoney, double carte, double creditDu) {
    }

    // --- helpers ---

    /** Prix unitaire selon le type de vente : prix de gros si défini et vente en gros, sinon détail. */
    private double resolveUnitPrice(Product product, SaleType saleType) {
        if (saleType == SaleType.GROS && product.getWholesalePrice() != null) {
            return product.getWholesalePrice();
        }
        if (product.getSalePrice() == null) {
            throw new IllegalArgumentException("Prix de vente non défini pour « " + product.getName() + " ».");
        }
        return product.getSalePrice();
    }

    private String generateReference(Long tenantId) {
        long next = saleRepository.countByTenantId(tenantId) + 1;
        return String.format("FAC-%06d", next);
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User user) {
            return user;
        }
        return null;
    }

    private Site ownedSite(Long id, Long tenantId) {
        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Site introuvable."));
        if (!site.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Site introuvable.");
        }
        return site;
    }

    private Customer ownedCustomer(Long id, Long tenantId) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Client introuvable."));
        if (!customer.getTenantId().equals(tenantId)) {
            throw new IllegalArgumentException("Client introuvable.");
        }
        return customer;
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
