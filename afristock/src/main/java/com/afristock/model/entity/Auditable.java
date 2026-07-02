package com.afristock.model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * Superclasse d'audit : renseigne automatiquement les dates de création et de dernière
 * modification (via {@code @EnableJpaAuditing}, voir {@code JpaConfig}).
 *
 * <p>Les nouvelles entités peuvent en hériter pour obtenir l'audit sans code répétitif. Les
 * entités historiques (Company, StockMovement…) pourront être migrées progressivement.</p>
 */
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
public abstract class Auditable {

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
