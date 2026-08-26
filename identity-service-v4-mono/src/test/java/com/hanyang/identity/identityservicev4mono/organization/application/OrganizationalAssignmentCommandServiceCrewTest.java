package com.hanyang.identity.identityservicev4mono.organization.application;


import com.hanyang.identity.identityservicev4mono.employee.domain.Employee;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeRepository;
import com.hanyang.identity.identityservicev4mono.organization.application.command.CreateOrganizationalAssignmentCommand;
import com.hanyang.identity.identityservicev4mono.organization.application.exception.CrewDepartmentMismatchException;
import com.hanyang.identity.identityservicev4mono.organization.application.exception.OrganizationReferenceDisabledException;
import com.hanyang.identity.identityservicev4mono.organization.domain.*;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class OrganizationalAssignmentCommandServiceCrewTest {

    private static final Clock CLOCK = Clock.fixed(
            Instant.parse("2026-08-25T00:00:00Z"),
            ZoneOffset.UTC
    );

    @Test
    void rejectsCrewFromDifferentDepartment() {
        OrganizationalAssignmentRepository assignmentRepository = mock(OrganizationalAssignmentRepository.class);
        EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
        DepartmentRepository departmentRepository = mock(DepartmentRepository.class);
        PositionRepository positionRepository = mock(PositionRepository.class);
        CrewRepository crewRepository = mock(CrewRepository.class);

        EmployeeId employeeId = EmployeeId.newId();
        DepartmentId assignmentDepartmentId = DepartmentId.newId();
        DepartmentId crewDepartmentId = DepartmentId.newId();
        PositionId positionId = PositionId.newId();
        CrewId crewId = CrewId.newId();

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(
                Employee.create(employeeId, "NV001", "Operator One")
        ));
        when(departmentRepository.findById(assignmentDepartmentId)).thenReturn(Optional.of(
                Department.create(assignmentDepartmentId, "OQC_SSD", "OQC SSD")
        ));
        when(positionRepository.findById(positionId)).thenReturn(Optional.of(
                Position.create(positionId, "OQC_OPERATOR", "OQC Operator")
        ));
        when(crewRepository.findById(crewId)).thenReturn(Optional.of(
                Crew.create(crewId, crewDepartmentId, "A", "Crew A")
        ));

        OrganizationalAssignmentCommandService service = new OrganizationalAssignmentCommandService(
                assignmentRepository,
                employeeRepository,
                departmentRepository,
                positionRepository,
                crewRepository,
                CLOCK
        );

        CreateOrganizationalAssignmentCommand command = new CreateOrganizationalAssignmentCommand(
                employeeId,
                assignmentDepartmentId,
                positionId,
                crewId,
                LocalDate.of(2026, 8, 25)
        );

        assertThrows(CrewDepartmentMismatchException.class, () -> service.assign(command));
    }

    @Test
    void rejectsDisabledCrew() {
        OrganizationalAssignmentRepository assignmentRepository = mock(OrganizationalAssignmentRepository.class);
        EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
        DepartmentRepository departmentRepository = mock(DepartmentRepository.class);
        PositionRepository positionRepository = mock(PositionRepository.class);
        CrewRepository crewRepository = mock(CrewRepository.class);

        EmployeeId employeeId = EmployeeId.newId();
        DepartmentId departmentId = DepartmentId.newId();
        PositionId positionId = PositionId.newId();
        CrewId crewId = CrewId.newId();
        Crew crew = Crew.create(crewId, departmentId, "A", "Crew A");
        crew.disable();

        when(employeeRepository.findById(employeeId)).thenReturn(Optional.of(
                Employee.create(employeeId, "NV002", "Operator Two")
        ));
        when(departmentRepository.findById(departmentId)).thenReturn(Optional.of(
                Department.create(departmentId, "OQC_SSD", "OQC SSD")
        ));
        when(positionRepository.findById(positionId)).thenReturn(Optional.of(
                Position.create(positionId, "OQC_OPERATOR", "OQC Operator")
        ));
        when(crewRepository.findById(crewId)).thenReturn(Optional.of(crew));

        OrganizationalAssignmentCommandService service = new OrganizationalAssignmentCommandService(
                assignmentRepository,
                employeeRepository,
                departmentRepository,
                positionRepository,
                crewRepository,
                CLOCK
        );

        CreateOrganizationalAssignmentCommand command = new CreateOrganizationalAssignmentCommand(
                employeeId,
                departmentId,
                positionId,
                crewId,
                LocalDate.of(2026, 8, 25)
        );

        assertThrows(OrganizationReferenceDisabledException.class, () -> service.assign(command));
    }
}