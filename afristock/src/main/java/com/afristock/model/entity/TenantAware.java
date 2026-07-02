package com.afristock.model.entity;


public interface TenantAware {
    Long getTenantId();
    void setTenantId(Long tenantId);
}