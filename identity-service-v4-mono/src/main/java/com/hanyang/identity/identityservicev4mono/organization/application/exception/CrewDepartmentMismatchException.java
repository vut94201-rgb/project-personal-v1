package com.hanyang.identity.identityservicev4mono.organization.application.exception;

import com.hanyang.identity.identityservicev4mono.organization.domain.CrewId;
import com.hanyang.identity.identityservicev4mono.organization.domain.DepartmentId;

public class CrewDepartmentMismatchException extends RuntimeException {
    public CrewDepartmentMismatchException(CrewId crewId, DepartmentId departmentId) {
        super("Crew " + crewId.value() + " does not belong to department " + departmentId.value());
    }
}