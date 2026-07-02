package com.afristock.repository;

import com.afristock.model.entity.AccountingEntry;
import com.afristock.model.enums.AccountType;
import com.afristock.model.enums.EntryType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AccountingEntryRepository extends JpaRepository<AccountingEntry, Long> {

    List<AccountingEntry> findByTenantIdOrderByEntryDateDescIdDesc(Long tenantId);

    @Query("SELECT COALESCE(SUM(e.amount), 0) FROM AccountingEntry e " +
            "WHERE e.tenantId = :tenantId AND e.type = :type")
    double sumByType(Long tenantId, EntryType type);

    @Query("SELECT COALESCE(SUM(CASE WHEN e.type = com.afristock.model.enums.EntryType.RECETTE THEN e.amount ELSE -e.amount END), 0) " +
            "FROM AccountingEntry e WHERE e.tenantId = :tenantId AND e.account = :account")
    double balanceForAccount(Long tenantId, AccountType account);
}
