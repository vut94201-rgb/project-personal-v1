package com.hanyang.identity.identityservicev4mono.employee.api.mapper;

import com.hanyang.identity.identityservicev4mono.employee.api.rest.request.CreateEmployeeRequest;
import com.hanyang.identity.identityservicev4mono.employee.api.rest.response.EmployeeResponse;
import com.hanyang.identity.identityservicev4mono.employee.application.command.CreateEmployeeCommand;
import com.hanyang.identity.identityservicev4mono.employee.domain.Employee;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EmployeeRestMapper {

    default CreateEmployeeCommand toCommand(
            CreateEmployeeRequest request
    ) {
        return new CreateEmployeeCommand(
                request.employeeCode(),
                request.fullName()
        );
    }

    default EmployeeResponse toResponse(Employee employee) {
        return new EmployeeResponse(
                employee.getId().value(),
                employee.getEmployeeCode(),
                employee.getFullName(),
                employee.getStatus().getCode()
        );
    }
}