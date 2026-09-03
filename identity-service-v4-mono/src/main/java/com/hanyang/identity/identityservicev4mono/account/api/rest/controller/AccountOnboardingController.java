package com.hanyang.identity.identityservicev4mono.account.api.rest.controller;

import com.hanyang.identity.identityservicev4mono.account.api.mapper.EmployeeOnboardingRestMapper;
import com.hanyang.identity.identityservicev4mono.account.api.rest.request.StartEmployeeOnboardingRequest;
import com.hanyang.identity.identityservicev4mono.account.api.rest.response.StartAccountOnboardingResponse;
import com.hanyang.identity.identityservicev4mono.employee.application.EmployeeOnboardingResult;
import com.hanyang.identity.identityservicev4mono.employee.application.onbroading.EmployeeOnboardingService;
import com.hanyang.identity.identityservicev4mono.security.authorization.IdentityAdminAccess;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/v1/account-onboardings")
@RequiredArgsConstructor
@IdentityAdminAccess
public class AccountOnboardingController {

    private final EmployeeOnboardingService onboardingService;
    private final EmployeeOnboardingRestMapper mapper;

    @PostMapping
    public ResponseEntity<StartAccountOnboardingResponse> start(
            @Valid @RequestBody StartEmployeeOnboardingRequest request) {

        EmployeeOnboardingResult result =
                onboardingService.start(mapper.toCommand(request));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mapper.toResponse(result));
    }
}