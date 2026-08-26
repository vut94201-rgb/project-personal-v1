package com.hanyang.identity.identityservicev4mono.organization.api.mapper;

import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import com.hanyang.identity.identityservicev4mono.organization.api.rest.request.CreateOrganizationalAssignmentRequest;
import com.hanyang.identity.identityservicev4mono.organization.api.rest.response.OrganizationalAssignmentResponse;
import com.hanyang.identity.identityservicev4mono.organization.application.command.CreateOrganizationalAssignmentCommand;
import com.hanyang.identity.identityservicev4mono.organization.domain.CrewId;
import com.hanyang.identity.identityservicev4mono.organization.domain.DepartmentId;
import com.hanyang.identity.identityservicev4mono.organization.domain.OrganizationalAssignment;
import com.hanyang.identity.identityservicev4mono.organization.domain.PositionId;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface OrganizationalAssignmentRestMapper {
    default CreateOrganizationalAssignmentCommand toCommand(CreateOrganizationalAssignmentRequest request) {
        return new CreateOrganizationalAssignmentCommand(
                new EmployeeId(request.employeeId()),
                new DepartmentId(request.departmentId()),
                new PositionId(request.positionId()),
                request.crewId() == null ? null : new CrewId(request.crewId()),
                request.effectiveFrom()
        );
    }

    default OrganizationalAssignmentResponse toResponse(OrganizationalAssignment assignment) {
        return new OrganizationalAssignmentResponse(
                assignment.getId().value(),
                assignment.getEmployeeId().value(),
                assignment.getDepartmentId().value(),
                assignment.getPositionId().value(),
                assignment.getCrewId() == null ? null : assignment.getCrewId().value(),
                assignment.getEffectiveFrom(),
                assignment.getEffectiveTo(),
                assignment.getStatus().getCode()
        );
    }
}