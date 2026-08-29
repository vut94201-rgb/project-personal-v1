package com.hanyang.identity.identityservicev4mono.service_identity.domain;

import com.hanyang.identity.identityservicev4mono.shared.persistence.StringCodeEnum;


public enum ServicePrincipalOwnershipType implements StringCodeEnum {

    PRIMARY("PRI"),
    TECHNICAL("TEC");

    private final String code;

    ServicePrincipalOwnershipType(String code) {
        this.code = code;
    }

    @Override
    public String getCode() {
        return code;
    }
}