package com.hanyang.identity.identityservicev4mono.organization.domain;

import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import lombok.Getter;

import java.time.LocalDate;
import java.util.Objects;

@Getter
public class OrganizationalAssignment {
    private final OrganizationalAssignmentId id;
    private final EmployeeId employeeId;
    private final DepartmentId departmentId;
    private final PositionId positionId;
    private final CrewId crewId;
    private final LocalDate effectiveFrom;

    private LocalDate effectiveTo;
    private OrganizationalAssignmentStatus status;

    private OrganizationalAssignment(
            OrganizationalAssignmentId id,
            EmployeeId employeeId,
            DepartmentId departmentId,
            PositionId positionId,
            CrewId crewId,
            LocalDate effectiveFrom,
            LocalDate effectiveTo,
            OrganizationalAssignmentStatus status
    ) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.employeeId = Objects.requireNonNull(employeeId, "employeeId must not be null");
        this.departmentId = Objects.requireNonNull(departmentId, "departmentId must not be null");
        this.positionId = Objects.requireNonNull(positionId, "positionId must not be null");
        this.crewId = crewId;
        this.effectiveFrom = Objects.requireNonNull(effectiveFrom, "effectiveFrom must not be null");
        this.effectiveTo = effectiveTo;
        this.status = Objects.requireNonNull(status, "status must not be null");
        validateDates();
        validateLifecycle();
    }

    public static OrganizationalAssignment create(
            OrganizationalAssignmentId id,
            EmployeeId employeeId,
            DepartmentId departmentId,
            PositionId positionId,
            CrewId crewId,
            LocalDate effectiveFrom
    ) {
        return new OrganizationalAssignment(
                id,
                employeeId,
                departmentId,
                positionId,
                crewId,
                effectiveFrom,
                null,
                OrganizationalAssignmentStatus.ACTIVE
        );
    }

    public static OrganizationalAssignment rehydrate(
            OrganizationalAssignmentId id,
            EmployeeId employeeId,
            DepartmentId departmentId,
            PositionId positionId,
            CrewId crewId,
            LocalDate effectiveFrom,
            LocalDate effectiveTo,
            OrganizationalAssignmentStatus status
    ) {
        return new OrganizationalAssignment(
                id, employeeId, departmentId, positionId, crewId,
                effectiveFrom, effectiveTo, status
        );
    }

    public void end(LocalDate endDate) {
        Objects.requireNonNull(endDate, "effectiveTo must not be null");
        if (status == OrganizationalAssignmentStatus.ENDED) {
            return;
        }
        if (endDate.isBefore(effectiveFrom)) {
            throw new IllegalArgumentException("effectiveTo must be on or after effectiveFrom");
        }
        this.effectiveTo = endDate;
        this.status = OrganizationalAssignmentStatus.ENDED;
    }

    private void validateDates() {
        if (effectiveTo != null && effectiveTo.isBefore(effectiveFrom)) {
            throw new IllegalArgumentException("effectiveTo must be on or after effectiveFrom");
        }
    }

    private void validateLifecycle() {
        if (status == OrganizationalAssignmentStatus.ACTIVE && effectiveTo != null) {
            throw new IllegalArgumentException("active assignment must not have effectiveTo");
        }
        if (status == OrganizationalAssignmentStatus.ENDED && effectiveTo == null) {
            throw new IllegalArgumentException("ended assignment must have effectiveTo");
        }
    }
}