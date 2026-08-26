package com.hanyang.identity.identityservicev4mono.organization.application.command;

import com.hanyang.identity.identityservicev4mono.organization.domain.OrganizationalAssignmentId;

import java.time.LocalDate;

public record EndOrganizationalAssignmentCommand(
        OrganizationalAssignmentId assignmentId,
        LocalDate effectiveTo
) {
}
