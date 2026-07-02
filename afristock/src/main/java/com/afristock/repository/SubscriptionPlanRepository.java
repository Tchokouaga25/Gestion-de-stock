package com.afristock.repository;

import com.afristock.model.entity.SubscriptionPlan;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SubscriptionPlanRepository extends JpaRepository<SubscriptionPlan, Long> {
    boolean existsByCode(String code);
    List<SubscriptionPlan> findAllByOrderByMonthlyPriceAsc();
}
