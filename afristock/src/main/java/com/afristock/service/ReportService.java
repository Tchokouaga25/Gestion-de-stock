package com.afristock.service;

import com.afristock.model.entity.Sale;
import com.afristock.model.entity.Site;
import com.afristock.model.entity.StockLevel;
import com.afristock.repository.SaleRepository;
import com.afristock.repository.SiteRepository;
import com.afristock.repository.StockLevelRepository;
import com.afristock.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Rapports (Phase 8) : rapport de fin de journée + exports Excel (Apache POI).
 *
 * <p>L'export PDF est assuré côté navigateur (impression → « Enregistrer en PDF ») sur les pages
 * de rapport et les reçus, ce qui évite une dépendance PDF serveur lourde.</p>
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportService {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private final SaleRepository saleRepository;
    private final StockLevelRepository stockLevelRepository;
    private final SiteRepository siteRepository;

    public DailyReport dailyReport(LocalDate date) {
        Long tenantId = TenantContext.getCurrentTenant();
        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to = from.plusDays(1);
        List<Sale> sales = saleRepository.findForPeriod(tenantId, from, to);
        double revenue = saleRepository.revenueForPeriod(tenantId, from, to);
        long itemsSold = saleRepository.itemsSoldForPeriod(tenantId, from, to);
        double grossProfit = saleRepository.grossProfitForPeriod(tenantId, from, to);
        List<SiteBreakdown> siteBreakdown = siteBreakdown(tenantId, from, to);
        return new DailyReport(date, sales, sales.size(), revenue, itemsSold, grossProfit, siteBreakdown);
    }

    private List<SiteBreakdown> siteBreakdown(Long tenantId, LocalDateTime from, LocalDateTime to) {
        Map<Long, Double> revenueBySite = new HashMap<>();
        for (Object[] row : saleRepository.revenueBySiteForPeriod(tenantId, from, to)) {
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
        List<Site> sites = siteRepository.findByTenantIdOrderByName(tenantId);
        return sites.stream()
                .map(site -> new SiteBreakdown(
                        site.getName(),
                        revenueBySite.getOrDefault(site.getId(), 0.0),
                        lowStockBySite.getOrDefault(site.getId(), 0L)))
                .toList();
    }

    public record DailyReport(LocalDate date, List<Sale> sales, int salesCount,
                              double revenue, long itemsSold, double grossProfit,
                              List<SiteBreakdown> siteBreakdown) {
    }

    public record SiteBreakdown(String siteName, double revenue, long lowStockCount) {
    }

    /** Export Excel des ventes d'une journée. */
    public byte[] salesExcel(LocalDate date) {
        Long tenantId = TenantContext.getCurrentTenant();
        LocalDateTime from = date.atStartOfDay();
        List<Sale> sales = saleRepository.findForPeriod(tenantId, from, from.plusDays(1));

        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Ventes");
            CellStyle header = headerStyle(wb);
            String[] cols = {"Facture", "Date", "Client", "Type", "Paiement", "Total", "Encaissé"};
            Row head = sheet.createRow(0);
            for (int i = 0; i < cols.length; i++) {
                Cell c = head.createCell(i);
                c.setCellValue(cols[i]);
                c.setCellStyle(header);
            }
            int r = 1;
            for (Sale s : sales) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(s.getReference());
                row.createCell(1).setCellValue(s.getSaleDate() != null ? s.getSaleDate().format(DT) : "");
                row.createCell(2).setCellValue(s.getCustomer() != null ? s.getCustomer().getName() : "Comptoir");
                row.createCell(3).setCellValue(s.getSaleType().name());
                row.createCell(4).setCellValue(s.getPaymentMethod().name());
                row.createCell(5).setCellValue(s.getTotalAmount());
                row.createCell(6).setCellValue(s.getAmountPaid());
            }
            for (int i = 0; i < cols.length; i++) sheet.autoSizeColumn(i);
            return toBytes(wb);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    /** Export Excel de l'état du stock (tous sites). */
    public byte[] stockExcel() {
        Long tenantId = TenantContext.getCurrentTenant();
        List<StockLevel> levels = stockLevelRepository.findAllForTenant(tenantId);
        try (Workbook wb = new XSSFWorkbook()) {
            Sheet sheet = wb.createSheet("Stock");
            CellStyle header = headerStyle(wb);
            String[] cols = {"Site", "Produit", "Quantité", "Seuil"};
            Row head = sheet.createRow(0);
            for (int i = 0; i < cols.length; i++) {
                Cell c = head.createCell(i);
                c.setCellValue(cols[i]);
                c.setCellStyle(header);
            }
            int r = 1;
            for (StockLevel l : levels) {
                Row row = sheet.createRow(r++);
                row.createCell(0).setCellValue(l.getSite().getName());
                row.createCell(1).setCellValue(l.getProduct().getName());
                row.createCell(2).setCellValue(l.getQuantity());
                row.createCell(3).setCellValue(l.getProduct().getMinThreshold() != null ? l.getProduct().getMinThreshold() : 0);
            }
            for (int i = 0; i < cols.length; i++) sheet.autoSizeColumn(i);
            return toBytes(wb);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private CellStyle headerStyle(Workbook wb) {
        CellStyle style = wb.createCellStyle();
        Font font = wb.createFont();
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private byte[] toBytes(Workbook wb) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            wb.write(out);
            return out.toByteArray();
        }
    }
}
