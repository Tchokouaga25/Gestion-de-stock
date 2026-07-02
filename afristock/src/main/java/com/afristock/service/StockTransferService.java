package com.afristock.service;

import com.afristock.model.entity.StockLevel;
import com.afristock.model.entity.StockTransfer;
import com.afristock.model.entity.User;
import com.afristock.repository.StockLevelRepository;
import com.afristock.repository.StockTransferRepository;
import com.afristock.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Transferts de stock entre sites.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class StockTransferService {

    private final StockTransferRepository transferRepository;
    private final StockLevelRepository stockLevelRepository;
    private final StockLevelService stockLevelService;

    @Transactional(readOnly = true)
    public List<StockTransfer> getHistory() {
        return transferRepository.findAllForTenant(TenantContext.getCurrentTenant());
    }

    public void transfer(Long productId, Long fromSiteId, Long toSiteId, Integer quantity, String reason) {
        if (fromSiteId == null || toSiteId == null || fromSiteId.equals(toSiteId)) {
            throw new IllegalArgumentException("Les sites source et destination doivent être différents.");
        }
        if (quantity == null || quantity <= 0) {
            throw new IllegalArgumentException("La quantité à transférer doit être supérieure à zéro.");
        }

        Long tenantId = TenantContext.getCurrentTenant();

        // Stock source : doit exister et être suffisant.
        StockLevel source = stockLevelRepository
                .findByProductIdAndSiteIdAndTenantId(productId, fromSiteId, tenantId)
                .orElseThrow(() -> new IllegalStateException("Aucun stock pour ce produit dans le site source."));
        if (source.getQuantity() < quantity) {
            throw new IllegalStateException("Stock insuffisant dans le site source.");
        }

        // Stock destination : créé si nécessaire.
        StockLevel dest = stockLevelService.getOrCreate(productId, toSiteId, tenantId);

        source.setQuantity(source.getQuantity() - quantity);
        dest.setQuantity(dest.getQuantity() + quantity);
        stockLevelRepository.save(source);
        stockLevelRepository.save(dest);

        StockTransfer transfer = new StockTransfer();
        transfer.setProduct(source.getProduct());
        transfer.setFromSite(source.getSite());
        transfer.setToSite(dest.getSite());
        transfer.setQuantity(quantity);
        transfer.setReason(reason);
        transfer.setUser(currentUser());
        transfer.setTenantId(tenantId);
        transferRepository.save(transfer);
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User user) {
            return user;
        }
        return null;
    }
}
