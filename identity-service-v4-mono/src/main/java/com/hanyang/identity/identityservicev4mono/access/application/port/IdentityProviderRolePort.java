package com.hanyang.identity.identityservicev4mono.access.application.port;

public interface IdentityProviderRolePort {

           ProvisionedRole synchronizeRole(
            String applicationCode,
            String roleCode,
            String roleName,
            boolean active
    );

            record ProvisionedRole(
            String externalId,
            String externalCode
    ) {
    }
}