package com.hanyang.identity.identityservicev4mono.service_identity.application.command;

import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;

import java.util.Objects;

public record CreateServicePrincipalCommand(
        String code,
        String displayName,
        String purpose,
        String description,
        EmployeeId primaryOwnerEmployeeId
) {
    public CreateServicePrincipalCommand {
        Objects.requireNonNull(primaryOwnerEmployeeId, "primaryOwnerEmployeeId must not be null");
    }
}