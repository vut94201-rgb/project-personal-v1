package com.hanyang.identity.identityservicev4mono.service_identity.application;


import com.hanyang.identity.identityservicev4mono.service_identity.application.provisioning.ServicePrincipalProvisioningState;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipal;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalOwner;

import java.util.List;
import java.util.Objects;

public record ServicePrincipalView(
        ServicePrincipal servicePrincipal,
        ServicePrincipalProvisioningState provisioning,
        List<ServicePrincipalOwner> activeOwners
) {
    public ServicePrincipalView {
        Objects.requireNonNull(servicePrincipal, "servicePrincipal must not be null");
        activeOwners = activeOwners == null ? List.of() : List.copyOf(activeOwners);
    }
}