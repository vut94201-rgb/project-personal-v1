package com.hanyang.identity.identityservicev4mono.account.domain;

import com.hanyang.identity.identityservicev4mono.shared.persistence.StringCodeEnum;

public enum AccountStatus implements StringCodeEnum {

    PENDING("PND"),
    ACTIVE("ACT"),
    DISABLED("DIS");

    private final String code;

    AccountStatus(String code) {
        this.code = code;
    }

    @Override
    public String getCode() {
        return code;
    }
}