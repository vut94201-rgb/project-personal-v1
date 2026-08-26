package com.hanyang.identity.identityservicev4mono.organization.application.command;

import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import com.hanyang.identity.identityservicev4mono.organization.domain.CrewId;
import com.hanyang.identity.identityservicev4mono.organization.domain.DepartmentId;
import com.hanyang.identity.identityservicev4mono.organization.domain.PositionId;
import jakarta.annotation.Nullable;

import java.time.LocalDate;


public record CreateOrganizationalAssignmentCommand(
        EmployeeId employeeId,
        DepartmentId departmentId,
        PositionId positionId,
        @Nullable CrewId crewId,
        LocalDate effectiveFrom
) {
}