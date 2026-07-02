package com.afristock;

import com.afristock.model.entity.Category;
import com.afristock.model.entity.Company;
import com.afristock.model.entity.Product;
import com.afristock.model.enums.CompanyStatus;
import jakarta.persistence.EntityManager;
import org.hibernate.Session;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Vérifie la propriété de sécurité la plus importante du SaaS : l'isolation des données entre
 * entreprises (tenants). Reproduit le cas qui était cassé par l'ancien StatementInspector :
 * une requête avec {@code ORDER BY} doit malgré tout rester filtrée par {@code company_id}.
 */
@DataJpaTest
class TenantIsolationTest {

    @Autowired
    private EntityManager em;

    @Autowired
    private TestEntityManager tem;

    @Test
    void leFiltreIsoleLesTenantsMemeAvecOrderBy() {
        Company a = company("Entreprise A", "a@test.com");
        Company b = company("Entreprise B", "b@test.com");
        tem.persist(a);
        tem.persist(b);

        Category catA = category("Boissons A", a.getId());
        Category catB = category("Boissons B", b.getId());
        tem.persist(catA);
        tem.persist(catB);

        tem.persist(product("REF-A1", "Jus A", catA, a.getId()));
        tem.persist(product("REF-A2", "Eau A", catA, a.getId()));
        tem.persist(product("REF-B1", "Jus B", catB, b.getId()));
        tem.flush();
        tem.clear();

        // On se place dans le contexte de l'entreprise A.
        Session session = em.unwrap(Session.class);
        session.enableFilter("tenantFilter").setParameter("tenantId", a.getId());

        // Requête AVEC tri : c'est exactement le cas qui fuyait auparavant.
        List<Product> result = em.createQuery(
                "select p from Product p order by p.name", Product.class).getResultList();

        assertThat(result).hasSize(2);
        assertThat(result).allMatch(p -> p.getTenantId().equals(a.getId()));
    }

    private Company company(String name, String email) {
        Company c = new Company();
        c.setName(name);
        c.setEmail(email);
        c.setStatus(CompanyStatus.ACTIVE);
        return c;
    }

    private Category category(String name, Long tenantId) {
        Category c = new Category();
        c.setName(name);
        c.setTenantId(tenantId);
        return c;
    }

    private Product product(String ref, String name, Category category, Long tenantId) {
        Product p = new Product();
        p.setReference(ref);
        p.setName(name);
        p.setCategory(category);
        p.setTenantId(tenantId);
        return p;
    }
}
