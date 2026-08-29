package com.hanyang.identity.identityservicev4mono.service_identity.domain;

import com.hanyang.identity.identityservicev4mono.shared.persistence.StringCodeEnum;

public enum ServicePrincipalOwnerStatus implements StringCodeEnum {

    ACTIVE("ACT"),
    REVOKED("REV");

    private final String code;

    ServicePrincipalOwnerStatus(String code) {
        this.code = code;
    }

    @Override
    public String getCode() {
        return code;
    }
}