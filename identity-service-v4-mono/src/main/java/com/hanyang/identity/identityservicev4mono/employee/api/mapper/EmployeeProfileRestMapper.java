package com.hanyang.identity.identityservicev4mono.employee.api.mapper;


import com.hanyang.identity.identityservicev4mono.employee.api.rest.request.UpdateEmployeeProfileRequest;
import com.hanyang.identity.identityservicev4mono.employee.api.rest.response.EmployeeProfileResponse;
import com.hanyang.identity.identityservicev4mono.employee.application.command.UpdateEmployeeProfileCommand;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeProfile;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface EmployeeProfileRestMapper {

    default UpdateEmployeeProfileCommand toCommand(
            EmployeeId employeeId,
            UpdateEmployeeProfileRequest request
    ) {
        return new UpdateEmployeeProfileCommand(
                employeeId,
                request.email(),
                request.phoneNumber(),
                request.address(),
                request.hireDate()
        );
    }

    default EmployeeProfileResponse toResponse(EmployeeProfile profile) {
        return new EmployeeProfileResponse(
                profile.getEmployeeId().value(),
                profile.getEmail(),
                profile.getPhoneNumber(),
                profile.getAddress(),
                profile.getHireDate()
        );
    }
}