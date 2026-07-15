package com.afristock.controller;

import com.afristock.model.entity.PurchaseOrder;
import com.afristock.model.entity.Sale;
import com.afristock.model.entity.Site;
import com.afristock.model.entity.StockLevel;
import com.afristock.model.entity.StockMovement;
import com.afristock.model.entity.User;
import com.afristock.model.enums.PurchaseStatus;
import com.afristock.repository.CustomerRepository;
import com.afristock.repository.ProductRepository;
import com.afristock.repository.PurchaseOrderRepository;
import com.afristock.repository.SaleRepository;
import com.afristock.repository.SiteRepository;
import com.afristock.repository.StockMovementRepository;
import com.afristock.repository.UserRepository;
import com.afristock.service.StockLevelService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

@Controller
@RequestMapping("/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final SaleRepository saleRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final StockLevelService stockLevelService;
    private final SiteRepository siteRepository;
    private final StockMovementRepository stockMovementRepository;

    /** Élément unifié d'activité récente (vente ou mouvement de stock), pour affichage chronologique. */
    public record ActivityItem(String type, String label, String detail, LocalDateTime at, Double amount) {}

    @GetMapping
    public String dashboard(Model model, Authentication authentication) {

        // Récupérer l'utilisateur connecté
        User user = (User) authentication.getPrincipal();

        // Charger l'utilisateur avec sa compagnie
        User userWithCompany = userRepository.findByIdWithCompany(user.getId());

        // Le Super-Administrateur n'a pas d'entreprise : on le dirige vers son espace dédié.
        if (userWithCompany.getCompany() == null) {
            return "redirect:/super-admin/companies";
        }
        Long tenantId = userWithCompany.getCompany().getId();

        // Statistiques réelles
        long totalProducts = productRepository.findByTenantId(tenantId).size();
        int totalStockUnits = stockLevelService.getAll().stream()
                .mapToInt(StockLevel::getQuantity)
                .sum();

        long totalActiveCustomers = customerRepository.findByTenantIdOrderByName(tenantId).stream()
                .filter(c -> c.isActive())
                .count();

        List<PurchaseOrder> purchaseOrders = purchaseOrderRepository.findAllForTenant(tenantId);
        long totalOrders = purchaseOrders.size();
        long ordersEnCours = purchaseOrders.stream()
                .filter(po -> po.getStatus() == PurchaseStatus.BROUILLON)
                .count();

        List<Sale> sales = saleRepository.findByTenantIdOrderBySaleDateDesc(tenantId);
        double totalRevenue = sales.stream().mapToDouble(Sale::getTotalAmount).sum();
        long invoiceCount = sales.size();

        List<StockLevel> lowStockLevels = stockLevelService.getLowStock();

        List<Sale> unpaidSales = sales.stream()
                .filter(s -> s.getBalanceDue() > 0)
                .toList();

        List<Sale> recentSales = sales.stream().limit(5).toList();

        List<Site> activeSites = siteRepository.findByTenantIdOrderByName(tenantId).stream()
                .filter(Site::isActive)
                .limit(3)
                .toList();

        double totalStockValue = stockLevelService.getTotalStockValue();

        List<StockMovement> recentMovements = stockMovementRepository.findByTenantIdOrderByCreatedAtDesc(tenantId)
                .stream().limit(5).toList();
        List<ActivityItem> recentActivity = Stream.concat(
                        recentSales.stream().map(s -> new ActivityItem(
                                "sale", s.getReference(),
                                s.getCustomer() != null ? s.getCustomer().getName() : "Comptoir",
                                s.getSaleDate(), s.getTotalAmount())),
                        recentMovements.stream().map(m -> new ActivityItem(
                                "movement", m.getProduct().getName(),
                                m.getType() + (m.getSite() != null ? " · " + m.getSite().getName() : ""),
                                m.getCreatedAt(), null)))
                .sorted(Comparator.comparing(ActivityItem::at, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(8)
                .toList();

        model.addAttribute("user", userWithCompany);
        model.addAttribute("companyName", userWithCompany.getCompany().getName());
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("totalStockUnits", totalStockUnits);
        model.addAttribute("totalActiveCustomers", totalActiveCustomers);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("ordersEnCours", ordersEnCours);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("invoiceCount", invoiceCount);
        model.addAttribute("lowStockLevels", lowStockLevels);
        model.addAttribute("unpaidSales", unpaidSales);
        model.addAttribute("recentSales", recentSales);
        model.addAttribute("activeSites", activeSites);
        model.addAttribute("totalStockValue", totalStockValue);
        model.addAttribute("recentActivity", recentActivity);
        model.addAttribute("welcomeMessage",
                "Bienvenue sur votre tableau de bord, " + userWithCompany.getFirstName());

        return "dashboard/index";
    }
}
