/**
 * Définition globale du filtre multi-tenant.
 *
 * <p>Le filtre {@code tenantFilter} est défini une seule fois ici (au niveau du package) et
 * appliqué sur chaque entité « tenant » via {@code @Filter(name = "tenantFilter")}.</p>
 *
 * <p>Il est activé automatiquement pour chaque requête par
 * {@link com.afristock.security.TenantFilterAspect}, qui injecte l'identifiant de l'entreprise
 * courante ({@link com.afristock.security.TenantContext}). Ceci garantit que toute requête JPQL /
 * Criteria / chargement de collection est filtrée par {@code company_id}, y compris les requêtes
 * triées, groupées ou paginées (ce que l'ancien StatementInspector ne faisait pas).</p>
 *
 * <p>⚠️ Limite connue d'Hibernate : les filtres ne s'appliquent PAS aux chargements directs par
 * identifiant ({@code EntityManager.find} / {@code repository.findById}). Pour ces accès, les
 * services doivent vérifier la propriété via {@code TenantContext} (voir les services métier).</p>
 */
@FilterDef(
        name = "tenantFilter",
        parameters = @ParamDef(name = "tenantId", type = Long.class),
        defaultCondition = "company_id = :tenantId"
)
package com.afristock.model.entity;

import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
