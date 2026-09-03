package com.hanyang.identity.identityservicev4mono.account.api.rest.controller;


import com.hanyang.identity.identityservicev4mono.account.api.mapper.EmployeeOnboardingRestMapper;
import com.hanyang.identity.identityservicev4mono.account.api.rest.request.StartEmployeeOnboardingRequest;
import com.hanyang.identity.identityservicev4mono.account.api.rest.response.StartAccountOnboardingResponse;
import com.hanyang.identity.identityservicev4mono.account.application.command.StartEmployeeOnboardingCommand;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountId;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountStatus;
import com.hanyang.identity.identityservicev4mono.employee.application.EmployeeOnboardingResult;
import com.hanyang.identity.identityservicev4mono.employee.application.onbroading.EmployeeOnboardingService;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeNationalIdentityId;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AccountOnboardingControllerTest {

    @Test
    void returnsCreatedResponseWithMaskedNationalIdentity() {
        EmployeeOnboardingService onboardingService = mock(EmployeeOnboardingService.class);
        EmployeeOnboardingRestMapper mapper = mock(EmployeeOnboardingRestMapper.class);

        AccountOnboardingController controller =
                new AccountOnboardingController(onboardingService, mapper);

        StartEmployeeOnboardingRequest request =
                new StartEmployeeOnboardingRequest(
                        "Nguyen Van A",
                        "nguyenvana",
                        "001204012345",
                        "a@example.com",
                        "0900000000",
                        "Hanoi",
                        LocalDate.of(2026, 8, 31)
                );

        StartEmployeeOnboardingCommand command =
                new StartEmployeeOnboardingCommand(
                        request.fullName(),
                        request.username(),
                        request.nationalIdentityNumber(),
                        request.email(),
                        request.phone(),
                        request.address(),
                        request.joinDate()
                );

        EmployeeOnboardingResult result =
                new EmployeeOnboardingResult(
                        new EmployeeId(UUID.randomUUID()),
                        "HY000042",
                        new EmployeeNationalIdentityId(UUID.randomUUID()),
                        "********2345",
                        new AccountId(UUID.randomUUID()),
                        "nguyenvana",
                        AccountStatus.PENDING
                );

        StartAccountOnboardingResponse response =
                new StartAccountOnboardingResponse(
                        result.accountId().value(),
                        result.employeeId().value(),
                        result.employeeCode(),
                        result.nationalIdentityId().value(),
                        result.maskedNationalIdentity(),
                        result.username(),
                        result.accountStatus()
                );

        when(mapper.toCommand(request)).thenReturn(command);
        when(onboardingService.start(command)).thenReturn(result);
        when(mapper.toResponse(result)).thenReturn(response);

        var actual = controller.start(request);

        assertEquals(HttpStatus.CREATED, actual.getStatusCode());
        assertSame(response, actual.getBody());
        assertEquals("********2345", actual.getBody().maskedNationalIdentity());

        verify(mapper).toCommand(request);
        verify(onboardingService).start(command);
        verify(mapper).toResponse(result);
    }
}