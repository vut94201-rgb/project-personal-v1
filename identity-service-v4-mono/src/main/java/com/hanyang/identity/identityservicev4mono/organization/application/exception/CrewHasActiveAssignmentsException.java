package com.hanyang.identity.identityservicev4mono.organization.application.exception;

import com.hanyang.identity.identityservicev4mono.organization.domain.CrewId;

public class CrewHasActiveAssignmentsException extends RuntimeException {
    public CrewHasActiveAssignmentsException(CrewId crewId) {
        super("Crew has active organizational assignments: " + crewId.value());
    }
}