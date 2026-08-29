package com.hanyang.identity.identityservicev4mono.service_identity.application.command;


import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalOwnerId;

import java.util.Objects;

public record RevokeServicePrincipalOwnerCommand(
        ServicePrincipalOwnerId ownerId
) {
    public RevokeServicePrincipalOwnerCommand {
        Objects.requireNonNull(ownerId, "ownerId must not be null");
    }
}