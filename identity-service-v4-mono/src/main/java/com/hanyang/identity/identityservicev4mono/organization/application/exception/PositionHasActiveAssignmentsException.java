package com.hanyang.identity.identityservicev4mono.organization.application.exception;

import com.hanyang.identity.identityservicev4mono.organization.domain.PositionId;

public class PositionHasActiveAssignmentsException extends RuntimeException {
    public PositionHasActiveAssignmentsException(PositionId id) {
        super("Position still has active organizational assignments: " + id.value());
    }
}
