package com.afristock.service;

import com.afristock.model.entity.Customer;
import com.afristock.repository.CustomerRepository;
import com.afristock.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Gestion des clients de l'entreprise courante.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CustomerService {

    private final CustomerRepository customerRepository;

    @Transactional(readOnly = true)
    public List<Customer> getAll() {
        return customerRepository.findByTenantIdOrderByName(TenantContext.getCurrentTenant());
    }

    @Transactional(readOnly = true)
    public List<Customer> search(String query) {
        return customerRepository.findByNameContainingIgnoreCaseAndTenantId(query, TenantContext.getCurrentTenant());
    }

    @Transactional(readOnly = true)
    public Customer getById(Long id) {
        return loadOwned(id);
    }

    public Customer save(Customer customer) {
        Long tenantId = TenantContext.getCurrentTenant();
        if (customer.getId() == null) {
            if (customerRepository.existsByNameAndTenantId(customer.getName(), tenantId)) {
                throw new IllegalArgumentException("Un client avec ce nom existe déjà.");
            }
            customer.setTenantId(tenantId);
            return customerRepository.save(customer);
        }
        Customer existing = loadOwned(customer.getId());
        existing.setName(customer.getName());
        existing.setType(customer.getType());
        existing.setEmail(customer.getEmail());
        existing.setPhone(customer.getPhone());
        existing.setAddress(customer.getAddress());
        existing.setCity(customer.getCity());
        existing.setCreditLimit(customer.getCreditLimit());
        existing.setNotes(customer.getNotes());
        existing.setActive(customer.isActive());
        // loyaltyPoints n'est pas modifiable manuellement ici (alimenté par les ventes).
        return customerRepository.save(existing);
    }

    public void delete(Long id) {
        customerRepository.delete(loadOwned(id));
    }

    private Customer loadOwned(Long id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Client introuvable."));
        if (!customer.getTenantId().equals(TenantContext.getCurrentTenant())) {
            throw new IllegalArgumentException("Client introuvable.");
        }
        return customer;
    }
}
