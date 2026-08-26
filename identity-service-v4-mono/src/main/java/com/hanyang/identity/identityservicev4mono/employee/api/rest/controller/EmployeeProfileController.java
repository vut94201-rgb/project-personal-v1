package com.hanyang.identity.identityservicev4mono.employee.api.rest.controller;

import com.hanyang.identity.identityservicev4mono.employee.api.mapper.EmployeeProfileRestMapper;
import com.hanyang.identity.identityservicev4mono.employee.api.rest.request.UpdateEmployeeProfileRequest;
import com.hanyang.identity.identityservicev4mono.employee.api.rest.response.EmployeeProfileResponse;
import com.hanyang.identity.identityservicev4mono.employee.application.EmployeeProfileCommandService;
import com.hanyang.identity.identityservicev4mono.employee.application.EmployeeProfileQueryService;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/employees/{employeeId}/profile")
@RequiredArgsConstructor
public class EmployeeProfileController {

    private final EmployeeProfileCommandService commandService;
    private final EmployeeProfileQueryService queryService;
    private final EmployeeProfileRestMapper mapper;

    @GetMapping
    public EmployeeProfileResponse get(@PathVariable UUID employeeId) {
        return mapper.toResponse(
                queryService.getByEmployeeId(new EmployeeId(employeeId))
        );
    }

    @PutMapping
    public EmployeeProfileResponse upsert(
            @PathVariable UUID employeeId,
            @Valid @RequestBody UpdateEmployeeProfileRequest request
    ) {
        EmployeeId id = new EmployeeId(employeeId);
        return mapper.toResponse(
                commandService.upsert(mapper.toCommand(id, request))
        );
    }
}