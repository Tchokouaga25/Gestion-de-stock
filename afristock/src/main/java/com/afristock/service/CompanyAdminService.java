package com.afristock.service;

import com.afristock.model.entity.Company;
import com.afristock.model.enums.CompanyStatus;
import com.afristock.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Administration des entreprises clientes, réservée au Super-Administrateur.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CompanyAdminService {

    private final CompanyRepository companyRepository;

    @Transactional(readOnly = true)
    public List<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    public void suspend(Long companyId) {
        setStatus(companyId, CompanyStatus.SUSPENDED);
    }

    public void activate(Long companyId) {
        setStatus(companyId, CompanyStatus.ACTIVE);
    }

    public void delete(Long companyId) {
        companyRepository.deleteById(companyId);
    }

    private void setStatus(Long companyId, CompanyStatus status) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Entreprise introuvable."));
        company.setStatus(status);
        company.setUpdatedAt(LocalDateTime.now());
        companyRepository.save(company);
    }
}
