package com.hanyang.identity.identityservicev4mono.organization.api.rest.response;

import java.time.LocalDate;
import java.util.UUID;

public record OrganizationalAssignmentResponse(
        UUID id,
        UUID employeeId,
        UUID departmentId,
        UUID positionId,
        UUID crewId,
        LocalDate effectiveFrom,
        LocalDate effectiveTo,
        String status
) {
}