package com.afristock.service;

import com.afristock.model.entity.Supplier;
import com.afristock.repository.SupplierRepository;
import com.afristock.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Gestion des fournisseurs de l'entreprise courante.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class SupplierService {

    private final SupplierRepository supplierRepository;

    @Transactional(readOnly = true)
    public List<Supplier> getAll() {
        return supplierRepository.findByTenantIdOrderByName(TenantContext.getCurrentTenant());
    }

    @Transactional(readOnly = true)
    public List<Supplier> search(String query) {
        return supplierRepository.findByNameContainingIgnoreCaseAndTenantId(query, TenantContext.getCurrentTenant());
    }

    @Transactional(readOnly = true)
    public Supplier getById(Long id) {
        return loadOwned(id);
    }

    public Supplier save(Supplier supplier) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (supplier.getId() == null) {
            if (supplierRepository.existsByNameAndTenantId(supplier.getName(), tenantId)) {
                throw new IllegalArgumentException("Un fournisseur avec ce nom existe déjà.");
            }
            supplier.setTenantId(tenantId);
            return supplierRepository.save(supplier);
        }
        Supplier existing = loadOwned(supplier.getId());
        existing.setName(supplier.getName());
        existing.setContactName(supplier.getContactName());
        existing.setEmail(supplier.getEmail());
        existing.setPhone(supplier.getPhone());
        existing.setAddress(supplier.getAddress());
        existing.setCity(supplier.getCity());
        existing.setNotes(supplier.getNotes());
        existing.setActive(supplier.isActive());
        return supplierRepository.save(existing);
    }

    public void delete(Long id) {
        supplierRepository.delete(loadOwned(id));
    }

    private Supplier loadOwned(Long id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Fournisseur introuvable."));
        if (!supplier.getTenantId().equals(TenantContext.getCurrentTenant())) {
            throw new IllegalArgumentException("Fournisseur introuvable.");
        }
        return supplier;
    }
}
