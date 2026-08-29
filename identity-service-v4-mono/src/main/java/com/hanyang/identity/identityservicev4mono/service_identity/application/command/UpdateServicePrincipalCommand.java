package com.hanyang.identity.identityservicev4mono.service_identity.application.command;


import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalId;

import java.util.Objects;

public record UpdateServicePrincipalCommand(
        ServicePrincipalId servicePrincipalId,
        String displayName,
        String purpose,
        String description
) {
    public UpdateServicePrincipalCommand {
        Objects.requireNonNull(servicePrincipalId, "servicePrincipalId must not be null");
    }
}