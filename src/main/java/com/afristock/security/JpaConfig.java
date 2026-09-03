package com.afristock.security;

import org.hibernate.cfg.AvailableSettings;
import org.springframework.boot.autoconfigure.orm.jpa.HibernatePropertiesCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JpaConfig {


    @Bean
    public HibernatePropertiesCustomizer hibernatePropertiesCustomizer(
            TenantStatementInspector tenantStatementInspector) {
        return hibernateProperties -> {
            // Optionnel : tu peux forcer d'autres props ici si besoin
            hibernateProperties.put(
                    AvailableSettings.STATEMENT_INSPECTOR,
                    tenantStatementInspector
            );
        };
    }

    // Si tu n'as pas encore ce bean, ajoute-le (pour activer l'inspector)
    @Bean
    public TenantStatementInspector tenantStatementInspector() {
        return new TenantStatementInspector();
    }
}