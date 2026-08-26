package com.hanyang.identity.identityservicev4mono.organization.domain;


import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class OrganizationalAssignmentCrewTest {

    @Test
    void crewIsOptionalForOrganizationalAssignment() {
        OrganizationalAssignment assignment = OrganizationalAssignment.create(
                OrganizationalAssignmentId.newId(),
                EmployeeId.newId(),
                DepartmentId.newId(),
                PositionId.newId(),
                null,
                LocalDate.of(2026, 8, 25)
        );

        assertNull(assignment.getCrewId());
        assertEquals(OrganizationalAssignmentStatus.ACTIVE, assignment.getStatus());
    }

    @Test
    void assignmentRetainsCrewAsPartOfHistoricalSnapshot() {
        CrewId crewId = CrewId.newId();

        OrganizationalAssignment assignment = OrganizationalAssignment.create(
                OrganizationalAssignmentId.newId(),
                EmployeeId.newId(),
                DepartmentId.newId(),
                PositionId.newId(),
                crewId,
                LocalDate.of(2026, 8, 25)
        );

        assignment.end(LocalDate.of(2026, 9, 1));

        assertEquals(crewId, assignment.getCrewId());
        assertEquals(OrganizationalAssignmentStatus.ENDED, assignment.getStatus());
        assertEquals(LocalDate.of(2026, 9, 1), assignment.getEffectiveTo());
    }
}