package com.hanyang.identity.identityservicev4mono.access.application.port;

public interface IdentityProviderAccessPort {

            void assignRole(
            String keycloakSubject,
            String applicationCode,
            String roleCode
    );

            void revokeRole(
            String keycloakSubject,
            String applicationCode,
            String roleCode
    );
}