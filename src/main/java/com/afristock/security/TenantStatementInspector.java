package com.afristock.security;

import org.hibernate.resource.jdbc.spi.StatementInspector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TenantStatementInspector implements StatementInspector {

    private static final Logger log = LoggerFactory.getLogger(TenantStatementInspector.class);

    @Override
    public String inspect(String sql) {
        // 1. Récupérer le tenant (ID de la compagnie) depuis le contexte
        Long tenantId = TenantContext.getCurrentTenant();

        // 2. IMPORTANT : Si pas de tenant (ex: Inscription ou Login), on ne touche à RIEN
        if (tenantId == null) {
            return sql;
        }

        String lowerSql = sql.toLowerCase();

        // 3. NE JAMAIS FILTRER LES INSERTS (L'id est déjà mis dans l'entité Java lors de la création)
        if (lowerSql.startsWith("insert")) {
            return sql;
        }

        // 4. Éviter de filtrer les tables système ou les séquences
        if (lowerSql.contains("pg_catalog") || lowerSql.contains("information_schema") || lowerSql.contains("nextval")) {
            return sql;
        }

        // 5. Appliquer le filtre de sécurité "company_id"
        try {
            // Pour SELECT, UPDATE, DELETE
            if (lowerSql.contains(" where ")) {
                // Si la requête a déjà un WHERE, on ajoute notre condition avec AND
                // On s'assure de ne pas casser la requête si elle finit par des clauses complexes
                if (!lowerSql.contains("order by") && !lowerSql.contains("group by") && !lowerSql.contains("limit")) {
                    return sql + " AND company_id = " + tenantId;
                }
            } else {
                // Si pas de WHERE, on doit l'ajouter intelligemment selon le type de requête
                if (lowerSql.startsWith("select") && !lowerSql.contains("order by") && !lowerSql.contains("group by") && !lowerSql.contains("limit")) {
                    return sql + " WHERE company_id = " + tenantId;
                } else if (lowerSql.startsWith("update") || lowerSql.startsWith("delete")) {
                    return sql + " WHERE company_id = " + tenantId;
                }
            }
        } catch (Exception e) {
            log.error("Erreur lors de l'inspection SQL : {}", e.getMessage());
        }

        return sql;
    }
}
