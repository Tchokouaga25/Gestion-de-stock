package com.afristock.service;

import com.afristock.model.entity.AccountingEntry;
import com.afristock.model.entity.User;
import com.afristock.model.enums.AccountType;
import com.afristock.model.enums.EntryType;
import com.afristock.repository.AccountingEntryRepository;
import com.afristock.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Comptabilité / trésorerie simplifiée (Phase 6).
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AccountingService {

    private final AccountingEntryRepository entryRepository;

    @Transactional(readOnly = true)
    public List<AccountingEntry> getJournal() {
        return entryRepository.findByTenantIdOrderByEntryDateDescIdDesc(TenantContext.getCurrentTenant());
    }

    public AccountingEntry addEntry(EntryType type, AccountType account, String category,
                                    Double amount, LocalDate date, String description) {
        if (amount == null || amount <= 0) {
            throw new IllegalArgumentException("Le montant doit être positif.");
        }
        AccountingEntry entry = new AccountingEntry();
        entry.setType(type);
        entry.setAccount(account);
        entry.setCategory(category);
        entry.setAmount(amount);
        entry.setEntryDate(date != null ? date : LocalDate.now());
        entry.setDescription(description);
        entry.setUser(currentUser());
        entry.setTenantId(TenantContext.getCurrentTenant());
        return entryRepository.save(entry);
    }

    @Transactional(readOnly = true)
    public Summary getSummary() {
        Long tenantId = TenantContext.getCurrentTenant();
        double recettes = entryRepository.sumByType(tenantId, EntryType.RECETTE);
        double depenses = entryRepository.sumByType(tenantId, EntryType.DEPENSE);
        double caisse = entryRepository.balanceForAccount(tenantId, AccountType.CAISSE);
        double banque = entryRepository.balanceForAccount(tenantId, AccountType.BANQUE);
        return new Summary(recettes, depenses, recettes - depenses, caisse, banque);
    }

    public record Summary(double recettes, double depenses, double resultat, double soldeCaisse, double soldeBanque) {
    }

    private User currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof User user) {
            return user;
        }
        return null;
    }
}
