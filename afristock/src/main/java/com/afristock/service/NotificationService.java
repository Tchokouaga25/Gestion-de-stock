package com.afristock.service;

import com.afristock.model.entity.Leave;
import com.afristock.model.entity.PurchaseOrder;
import com.afristock.model.entity.Sale;
import com.afristock.model.entity.StockLevel;
import com.afristock.model.enums.LeaveStatus;
import com.afristock.model.enums.PurchaseStatus;
import com.afristock.repository.LeaveRepository;
import com.afristock.repository.PurchaseOrderRepository;
import com.afristock.repository.SaleRepository;
import com.afristock.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Agrège à la volée les signaux d'alerte déjà calculés ailleurs dans l'application (stock bas,
 * congés en attente, commandes en brouillon, ventes impayées) pour alimenter la cloche de
 * notifications de la topbar. Aucune donnée n'est persistée : tout est recalculé par requête.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private static final int MAX_ITEMS_PER_TYPE = 3;

    private final StockLevelService stockLevelService;
    private final LeaveRepository leaveRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SaleRepository saleRepository;
    private final FeatureService featureService;

    public record NotificationItem(String type, String icon, String label, String detail, String link) {}

    public record NotificationSummary(int count, List<NotificationItem> items) {}

    public NotificationSummary getSummary() {
        Long tenantId = TenantContext.getCurrentTenant();
        List<NotificationItem> items = new ArrayList<>();
        int count = 0;

        if (featureService.isEnabled("STOCK")) {
            List<StockLevel> lowStock = stockLevelService.getLowStock();
            count += lowStock.size();
            lowStock.stream().limit(MAX_ITEMS_PER_TYPE).forEach(sl -> items.add(new NotificationItem(
                    "stock", "package-x",
                    sl.getProduct().getName() + " — " + sl.getSite().getName(),
                    "Stock bas : " + sl.getQuantity() + " / seuil " + sl.getProduct().getMinThreshold(),
                    "/stock")));
        }

        if (featureService.isEnabled("HR")) {
            long pendingLeaves = leaveRepository.countByTenantIdAndStatus(tenantId, LeaveStatus.EN_ATTENTE);
            count += pendingLeaves;
            leaveRepository.findByTenantIdAndStatusOrderByStartDateDesc(tenantId, LeaveStatus.EN_ATTENTE, PageRequest.of(0, MAX_ITEMS_PER_TYPE))
                    .forEach(l -> items.add(new NotificationItem(
                            "leave", "calendar-clock",
                            l.getEmployee().getFullName(),
                            "Congé en attente de validation (" + l.getStartDate() + " → " + l.getEndDate() + ")",
                            "/hr/employees")));
        }

        if (featureService.isEnabled("PURCHASES")) {
            long draftOrders = purchaseOrderRepository.countByTenantIdAndStatus(tenantId, PurchaseStatus.BROUILLON);
            count += draftOrders;
            purchaseOrderRepository.findByTenantIdAndStatusOrderByOrderDateDesc(tenantId, PurchaseStatus.BROUILLON, PageRequest.of(0, MAX_ITEMS_PER_TYPE))
                    .forEach((PurchaseOrder po) -> items.add(new NotificationItem(
                            "purchase", "shopping-cart",
                            po.getReference() + " — " + po.getSupplier().getName(),
                            "Commande en brouillon, non encore validée",
                            "/purchases")));
        }

        if (featureService.isEnabled("SALES")) {
            long unpaidSales = saleRepository.countUnpaidByTenantId(tenantId);
            count += unpaidSales;
            saleRepository.findUnpaidByTenantId(tenantId, PageRequest.of(0, MAX_ITEMS_PER_TYPE))
                    .forEach((Sale s) -> items.add(new NotificationItem(
                            "sale", "receipt",
                            s.getReference() + (s.getCustomer() != null ? " — " + s.getCustomer().getName() : ""),
                            "Solde impayé : " + (s.getTotalAmount() - s.getAmountPaid()),
                            "/sales")));
        }

        return new NotificationSummary(count, items);
    }
}
