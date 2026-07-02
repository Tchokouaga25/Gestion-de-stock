package com.afristock.security;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.hibernate.Session;
import org.springframework.stereotype.Component;

/**
 * Active le filtre multi-tenant Hibernate ({@code tenantFilter}) sur la session courante avant
 * chaque appel à un repository.
 *
 * <p>Remplace l'ancien {@code TenantStatementInspector} (concaténation de chaîne SQL) qui laissait
 * fuiter les données entre entreprises dès qu'une requête contenait {@code order by},
 * {@code group by} ou {@code limit}. Le filtre Hibernate, lui, s'applique correctement à toutes les
 * requêtes JPQL / Criteria, y compris triées, groupées et paginées.</p>
 *
 * <p>Quand aucun tenant n'est défini (inscription, login, ou Super-Administrateur qui doit voir
 * toutes les entreprises), le filtre n'est pas activé.</p>
 */
@Aspect
@Component
public class TenantFilterAspect {

    @PersistenceContext
    private EntityManager entityManager;

    @Before("execution(* com.afristock.repository..*(..))")
    public void enableTenantFilter() {
        Long tenantId = TenantContext.getCurrentTenant();
        if (tenantId == null) {
            // Contexte public ou Super-Administrateur : pas de filtrage par entreprise.
            return;
        }
        Session session = entityManager.unwrap(Session.class);
        session.enableFilter("tenantFilter").setParameter("tenantId", tenantId);
    }
}
