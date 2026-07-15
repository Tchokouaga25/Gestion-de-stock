package com.afristock.service;

import com.afristock.model.entity.Site;
import com.afristock.repository.EmployeeRepository;
import com.afristock.repository.SaleRepository;
import com.afristock.repository.SiteRepository;
import com.afristock.repository.StockLevelRepository;
import com.afristock.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Gestion des sites (boutiques / entrepôts) de l'entreprise courante.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SiteService {

    private final SiteRepository siteRepository;
    private final EmployeeRepository employeeRepository;
    private final SaleRepository saleRepository;
    private final StockLevelRepository stockLevelRepository;

    public record SiteStats(Site site, double revenueThisMonth, long lowStockAlerts, long staffCount) {}

    @Transactional(readOnly = true)
    public List<Site> getAll() {
        return siteRepository.findByTenantIdOrderByName(TenantContext.getCurrentTenant());
    }

    /** Cartes boutiques avec KPIs agrégés (ventes du mois, alertes stock, personnel), sans N+1. */
    @Transactional(readOnly = true)
    public List<SiteStats> getSiteCardStats() {
        Long tenantId = TenantContext.getCurrentTenant();
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();
        LocalDateTime monthEnd = monthStart.plusMonths(1);

        Map<Long, Double> revenueBySite = new HashMap<>();
        for (Object[] row : saleRepository.revenueBySiteForPeriod(tenantId, monthStart, monthEnd)) {
            if (row[0] != null) {
                revenueBySite.put((Long) row[0], ((Number) row[1]).doubleValue());
            }
        }
        Map<Long, Long> lowStockBySite = new HashMap<>();
        for (Object[] row : stockLevelRepository.countLowStockBySite(tenantId)) {
            if (row[0] != null) {
                lowStockBySite.put((Long) row[0], ((Number) row[1]).longValue());
            }
        }

        return getAll().stream()
                .map(site -> new SiteStats(
                        site,
                        revenueBySite.getOrDefault(site.getId(), 0.0),
                        lowStockBySite.getOrDefault(site.getId(), 0L),
                        employeeRepository.countBySiteIdAndTenantId(site.getId(), tenantId)))
                .toList();
    }

    @Transactional(readOnly = true)
    public Site getById(Long id) {
        return loadOwned(id);
    }

    public Site save(Site site) {
        Long tenantId = TenantContext.getCurrentTenant();

        if (site.getId() == null) {
            if (siteRepository.existsByNameAndTenantId(site.getName(), tenantId)) {
                throw new IllegalArgumentException("Un site avec ce nom existe déjà.");
            }
            site.setTenantId(tenantId);
        } else {
            // Mise à jour : on recharge le site existant pour préserver son tenant et appliquer
            // les champs modifiables uniquement (jamais le company_id).
            Site existing = loadOwned(site.getId());
            existing.setName(site.getName());
            existing.setType(site.getType());
            existing.setAddress(site.getAddress());
            existing.setCity(site.getCity());
            existing.setPhone(site.getPhone());
            existing.setActive(site.isActive());
            return siteRepository.save(existing);
        }
        return siteRepository.save(site);
    }

    public void delete(Long id) {
        Site site = loadOwned(id);
        siteRepository.delete(site);
    }

    /**
     * Charge un site en vérifiant qu'il appartient à l'entreprise courante (le filtre Hibernate
     * ne s'applique pas au findById).
     */
    private Site loadOwned(Long id) {
        Site site = siteRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Site introuvable."));
        if (!site.getTenantId().equals(TenantContext.getCurrentTenant())) {
            throw new IllegalArgumentException("Site introuvable.");
        }
        return site;
    }
}
