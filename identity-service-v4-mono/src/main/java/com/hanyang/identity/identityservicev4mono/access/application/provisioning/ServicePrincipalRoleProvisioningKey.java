package com.hanyang.identity.identityservicev4mono.access.application.provisioning;


import com.hanyang.identity.identityservicev4mono.access.domain.RoleId;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalId;

import java.util.UUID;

public record ServicePrincipalRoleProvisioningKey(
        ServicePrincipalId servicePrincipalId,
        RoleId roleId
) {
    private static final String DELIMITER = ":";

    public String serialize() {
        return servicePrincipalId.value() + DELIMITER + roleId.value();
    }

    public static ServicePrincipalRoleProvisioningKey parse(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Service-principal-role aggregate id must not be blank"
            );
        }

        String[] parts = value.trim().split(DELIMITER, -1);
        if (parts.length != 2) {
            throw new IllegalArgumentException(
                    "Invalid service-principal-role aggregate id: " + value
            );
        }

        try {
            return new ServicePrincipalRoleProvisioningKey(
                    new ServicePrincipalId(UUID.fromString(parts[0])),
                    new RoleId(UUID.fromString(parts[1]))
            );
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid service-principal-role aggregate id: " + value,
                    exception
            );
        }
    }
}