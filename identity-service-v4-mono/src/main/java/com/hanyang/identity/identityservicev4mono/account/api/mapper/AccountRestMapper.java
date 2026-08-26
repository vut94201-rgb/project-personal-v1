package com.hanyang.identity.identityservicev4mono.account.api.mapper;

import com.hanyang.identity.identityservicev4mono.account.api.rest.request.CreateAccountRequest;
import com.hanyang.identity.identityservicev4mono.account.api.rest.response.AccountResponse;
import com.hanyang.identity.identityservicev4mono.account.application.command.CreateAccountCommand;
import com.hanyang.identity.identityservicev4mono.account.domain.Account;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import org.mapstruct.Mapper;


@Mapper(componentModel = "spring")
public interface AccountRestMapper {

    default CreateAccountCommand toCommand(
            CreateAccountRequest request
    ) {
        return new CreateAccountCommand(
                new EmployeeId(request.employeeId()),
                request.username()
        );
    }

    default AccountResponse toResponse(Account account) {
        return new AccountResponse(
                account.getId().value(),
                account.getEmployeeId().value(),
                account.getUsername(),
                account.getStatus().getCode()
        );
    }
}