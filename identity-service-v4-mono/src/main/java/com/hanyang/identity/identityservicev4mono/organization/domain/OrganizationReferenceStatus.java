package com.hanyang.identity.identityservicev4mono.organization.domain;

import com.hanyang.identity.identityservicev4mono.shared.persistence.StringCodeEnum;

import java.util.Locale;

public enum OrganizationReferenceStatus implements StringCodeEnum {
    ACTIVE("ACT"),
    DISABLED("DIS");

    private final String code;

    OrganizationReferenceStatus(String code) {
        this.code = code;
    }

    @Override
    public String getCode() {
        return code;
    }

    public static OrganizationReferenceStatus fromExternalValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        String normalized = value.trim().toUpperCase(Locale.ROOT);
        for (OrganizationReferenceStatus status : values()) {
            if (status.name().equals(normalized) || status.code.equals(normalized)) {
                return status;
            }
        }
        throw new IllegalArgumentException("Unknown organization status: " + value);
    }
}
