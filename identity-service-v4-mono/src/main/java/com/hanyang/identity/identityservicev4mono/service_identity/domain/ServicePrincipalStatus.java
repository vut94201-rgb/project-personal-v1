package com.hanyang.identity.identityservicev4mono.service_identity.domain;

import com.hanyang.identity.identityservicev4mono.shared.persistence.StringCodeEnum;

public enum ServicePrincipalStatus implements StringCodeEnum {

    PENDING("PND"),
    ACTIVE("ACT"),
    DISABLED("DIS");

    private final String code;

    ServicePrincipalStatus(String code) {
        this.code = code;
    }

    @Override
    public String getCode() {
        return code;
    }
}