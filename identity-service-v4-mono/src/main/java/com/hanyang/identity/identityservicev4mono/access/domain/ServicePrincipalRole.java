package com.hanyang.identity.identityservicev4mono.access.domain;


import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalId;
import lombok.Getter;

import java.util.Objects;

@Getter
public class ServicePrincipalRole {

    private final ServicePrincipalId servicePrincipalId;
    private final RoleId roleId;

    private ServicePrincipalRole(
            ServicePrincipalId servicePrincipalId,
            RoleId roleId
    ) {
        this.servicePrincipalId = Objects.requireNonNull(
                servicePrincipalId,
                "servicePrincipalId must not be null"
        );
        this.roleId = Objects.requireNonNull(
                roleId,
                "roleId must not be null"
        );
    }

    public static ServicePrincipalRole create(
            ServicePrincipalId servicePrincipalId,
            RoleId roleId
    ) {
        return new ServicePrincipalRole(servicePrincipalId, roleId);
    }

    public static ServicePrincipalRole rehydrate(
            ServicePrincipalId servicePrincipalId,
            RoleId roleId
    ) {
        return new ServicePrincipalRole(servicePrincipalId, roleId);
    }
}