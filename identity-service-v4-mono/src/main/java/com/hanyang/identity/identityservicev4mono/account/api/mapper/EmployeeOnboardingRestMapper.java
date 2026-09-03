package com.hanyang.identity.identityservicev4mono.account.api.mapper;

import com.hanyang.identity.identityservicev4mono.account.api.rest.request.StartEmployeeOnboardingRequest;
import com.hanyang.identity.identityservicev4mono.account.api.rest.response.StartAccountOnboardingResponse;
import com.hanyang.identity.identityservicev4mono.account.application.command.StartEmployeeOnboardingCommand;
import com.hanyang.identity.identityservicev4mono.employee.application.EmployeeOnboardingResult;
import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

@Mapper(componentModel = "spring")
public interface EmployeeOnboardingRestMapper {
  default StartEmployeeOnboardingCommand toCommand(StartEmployeeOnboardingRequest request) {
    return new StartEmployeeOnboardingCommand(
            request.fullName(),
            request.username(),
            request.nationalIdentityNumber(),
            request.email(),
            request.phone(),
            request.address(),
            request.joinDate());
  }

  default StartAccountOnboardingResponse toResponse(EmployeeOnboardingResult result) {
    return new StartAccountOnboardingResponse(
            result.accountId().value(),
            result.employeeId().value(),
            result.employeeCode(),
            result.nationalIdentityId().value(),
            result.maskedNationalIdentity(),
            result.username(),
            result.accountStatus()
    );
  }
}
