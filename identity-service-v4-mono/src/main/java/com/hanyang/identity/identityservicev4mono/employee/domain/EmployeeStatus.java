package com.hanyang.identity.identityservicev4mono.employee.domain;

import com.hanyang.identity.identityservicev4mono.shared.persistence.StringCodeEnum;

public enum EmployeeStatus implements StringCodeEnum {

    ACTIVE("ACT"),
    INACTIVE("INA"),
    TERMINATED("TER");

    private final String code;

    EmployeeStatus(String code) {
        this.code = code;
    }

    @Override
    public String getCode() {
        return code;
    }
}