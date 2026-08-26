package com.hanyang.identity.identityservicev4mono.organization.api.mapper;

import com.hanyang.identity.identityservicev4mono.organization.api.rest.request.CreateDepartmentRequest;
import com.hanyang.identity.identityservicev4mono.organization.api.rest.response.DepartmentResponse;
import com.hanyang.identity.identityservicev4mono.organization.application.command.CreateDepartmentCommand;
import com.hanyang.identity.identityservicev4mono.organization.domain.Department;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DepartmentRestMapper {
    default CreateDepartmentCommand toCommand(CreateDepartmentRequest request) {
        return new CreateDepartmentCommand(request.code(), request.name());
    }

    default DepartmentResponse toResponse(Department department) {
        return new DepartmentResponse(
                department.getId().value(),
                department.getCode(),
                department.getName(),
                department.getStatus().getCode()
        );
    }
}
