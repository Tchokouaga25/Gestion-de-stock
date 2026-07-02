package com.afristock.controller;

import com.afristock.model.enums.AccountType;
import com.afristock.model.enums.EntryType;
import com.afristock.service.AccountingService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;

@Controller
@RequestMapping("/accounting")
@RequiredArgsConstructor
public class AccountingController {

    private final AccountingService accountingService;

    @GetMapping
    @PreAuthorize("hasAuthority('ACCOUNTING_READ')")
    public String journal(Model model) {
        model.addAttribute("entries", accountingService.getJournal());
        model.addAttribute("summary", accountingService.getSummary());
        model.addAttribute("types", EntryType.values());
        model.addAttribute("accounts", AccountType.values());
        return "accounting/journal";
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ACCOUNTING_WRITE')")
    public String add(@RequestParam EntryType type,
                      @RequestParam AccountType account,
                      @RequestParam(required = false) String category,
                      @RequestParam Double amount,
                      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate entryDate,
                      @RequestParam(required = false) String description,
                      RedirectAttributes ra) {
        try {
            accountingService.addEntry(type, account, category, amount, entryDate, description);
            ra.addFlashAttribute("success", "Écriture enregistrée.");
        } catch (Exception e) {
            ra.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/accounting";
    }
}
