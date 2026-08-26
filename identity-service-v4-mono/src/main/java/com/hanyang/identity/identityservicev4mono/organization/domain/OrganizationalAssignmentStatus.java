package com.hanyang.identity.identityservicev4mono.organization.domain;

import com.hanyang.identity.identityservicev4mono.shared.persistence.StringCodeEnum;

public enum OrganizationalAssignmentStatus implements StringCodeEnum {
    ACTIVE("ACT"),
    ENDED("END");

    private final String code;

    OrganizationalAssignmentStatus(String code) {
        this.code = code;
    }

    @Override
    public String getCode() {
        return code;
    }
}
