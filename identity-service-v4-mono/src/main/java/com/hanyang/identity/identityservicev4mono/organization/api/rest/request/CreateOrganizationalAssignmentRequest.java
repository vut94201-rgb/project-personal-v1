package com.hanyang.identity.identityservicev4mono.organization.api.rest.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.UUID;
public record CreateOrganizationalAssignmentRequest(
        @NotNull UUID employeeId,
        @NotNull UUID departmentId,
        @NotNull UUID positionId,
        UUID crewId,
        @NotNull LocalDate effectiveFrom
) {
}