package com.afristock.controller;

import com.afristock.service.ReportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;

@Controller
@RequestMapping("/reports")
@RequiredArgsConstructor
public class ReportController {

    private static final String XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

    private final ReportService reportService;

    @GetMapping
    @PreAuthorize("hasAuthority('REPORT_READ')")
    public String daily(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                        Model model) {
        LocalDate day = date != null ? date : LocalDate.now();
        model.addAttribute("report", reportService.dailyReport(day));
        return "reports/daily";
    }

    @GetMapping("/sales.xlsx")
    @PreAuthorize("hasAuthority('REPORT_READ')")
    public ResponseEntity<byte[]> salesExcel(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        LocalDate day = date != null ? date : LocalDate.now();
        byte[] data = reportService.salesExcel(day);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=ventes-" + day + ".xlsx")
                .contentType(MediaType.parseMediaType(XLSX))
                .body(data);
    }

    @GetMapping("/stock.xlsx")
    @PreAuthorize("hasAuthority('REPORT_READ')")
    public ResponseEntity<byte[]> stockExcel() {
        byte[] data = reportService.stockExcel();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=stock.xlsx")
                .contentType(MediaType.parseMediaType(XLSX))
                .body(data);
    }
}
