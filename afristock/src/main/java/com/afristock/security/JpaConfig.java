package com.afristock.security;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * Configuration JPA.
 *
 * <p>Active l'audit JPA ({@code @CreatedDate}, {@code @LastModifiedDate}, etc.).</p>
 *
 * <p>L'isolation multi-tenant n'est plus assurée par un StatementInspector (supprimé car peu sûr)
 * mais par le filtre Hibernate {@code tenantFilter} activé par
 * {@link TenantFilterAspect}.</p>
 */
@Configuration
@EnableJpaAuditing
public class JpaConfig {
}
