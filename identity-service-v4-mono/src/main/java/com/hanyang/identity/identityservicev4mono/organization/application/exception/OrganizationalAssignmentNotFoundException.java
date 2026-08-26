package com.hanyang.identity.identityservicev4mono.organization.application.exception;

import com.hanyang.identity.identityservicev4mono.organization.domain.OrganizationalAssignmentId;

public class OrganizationalAssignmentNotFoundException extends RuntimeException {
    public OrganizationalAssignmentNotFoundException(OrganizationalAssignmentId id) {
        super("Organizational assignment not found: " + id.value());
    }
}
