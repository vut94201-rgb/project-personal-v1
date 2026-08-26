package com.hanyang.identity.identityservicev4mono.organization.api.rest.request;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record EndOrganizationalAssignmentRequest(
        @NotNull LocalDate effectiveTo
) {
}
