package com.hanyang.identity.identityservicev4mono.access.application;

import com.hanyang.identity.identityservicev4mono.access.application.provisioning.ServicePrincipalRoleProvisioningState;
import com.hanyang.identity.identityservicev4mono.access.domain.Role;

import java.util.Objects;

public record ServicePrincipalRoleAssignmentView(
        Role role,
        ServicePrincipalRoleProvisioningState provisioning
) {
    public ServicePrincipalRoleAssignmentView {
        Objects.requireNonNull(role, "role must not be null");
    }
}