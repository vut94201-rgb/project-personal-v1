package com.hanyang.identity.identityservicev4mono.access.domain;


import com.hanyang.identity.identityservicev4mono.shared.persistence.StringCodeEnum;

public enum ApplicationStatus implements StringCodeEnum {

    ACTIVE("ACT"),
    DISABLED("DIS");

    private final String code;

    ApplicationStatus(String code) {
        this.code = code;
    }

    @Override
    public String getCode() {
        return code;
    }
}