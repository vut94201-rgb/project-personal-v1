package com.hanyang.identity.identityservicev4mono.organization.api.rest.controller;

import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import com.hanyang.identity.identityservicev4mono.organization.api.mapper.OrganizationalAssignmentRestMapper;
import com.hanyang.identity.identityservicev4mono.organization.api.rest.request.CreateOrganizationalAssignmentRequest;
import com.hanyang.identity.identityservicev4mono.organization.api.rest.request.EndOrganizationalAssignmentRequest;
import com.hanyang.identity.identityservicev4mono.organization.api.rest.response.OrganizationalAssignmentResponse;
import com.hanyang.identity.identityservicev4mono.organization.application.OrganizationalAssignmentCommandService;
import com.hanyang.identity.identityservicev4mono.organization.application.OrganizationalAssignmentQueryService;
import com.hanyang.identity.identityservicev4mono.organization.application.command.EndOrganizationalAssignmentCommand;
import com.hanyang.identity.identityservicev4mono.organization.domain.CrewId;
import com.hanyang.identity.identityservicev4mono.organization.domain.DepartmentId;
import com.hanyang.identity.identityservicev4mono.organization.domain.OrganizationalAssignmentId;
import com.hanyang.identity.identityservicev4mono.organization.domain.PositionId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/organizational-assignments")
@RequiredArgsConstructor
public class OrganizationalAssignmentController {
    private final OrganizationalAssignmentCommandService commandService;
    private final OrganizationalAssignmentQueryService queryService;
    private final OrganizationalAssignmentRestMapper mapper;

    @PostMapping
    public ResponseEntity<OrganizationalAssignmentResponse> assign(
            @Valid @RequestBody CreateOrganizationalAssignmentRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toResponse(commandService.assign(mapper.toCommand(request))));
    }

    @GetMapping("/{id}")
    public OrganizationalAssignmentResponse getById(@PathVariable UUID id) {
        return mapper.toResponse(queryService.getById(new OrganizationalAssignmentId(id)));
    }

    @GetMapping("/employee/{employeeId}/active")
    public ResponseEntity<OrganizationalAssignmentResponse> getActiveByEmployee(
            @PathVariable UUID employeeId
    ) {
        return queryService.findActiveByEmployeeId(new EmployeeId(employeeId))
                .map(mapper::toResponse)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/employee/{employeeId}/history")
    public List<OrganizationalAssignmentResponse> getHistoryByEmployee(@PathVariable UUID employeeId) {
        return queryService.findHistoryByEmployeeId(new EmployeeId(employeeId))
                .stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/department/{departmentId}/active")
    public List<OrganizationalAssignmentResponse> getActiveByDepartment(@PathVariable UUID departmentId) {
        return queryService.findActiveByDepartmentId(new DepartmentId(departmentId))
                .stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/position/{positionId}/active")
    public List<OrganizationalAssignmentResponse> getActiveByPosition(@PathVariable UUID positionId) {
        return queryService.findActiveByPositionId(new PositionId(positionId))
                .stream().map(mapper::toResponse).toList();
    }

    @GetMapping("/crew/{crewId}/active")
    public List<OrganizationalAssignmentResponse> getActiveByCrew(@PathVariable UUID crewId) {
        return queryService.findActiveByCrewId(new CrewId(crewId))
                .stream().map(mapper::toResponse).toList();
    }

    @PatchMapping("/{id}/end")
    public OrganizationalAssignmentResponse end(
            @PathVariable UUID id,
            @Valid @RequestBody EndOrganizationalAssignmentRequest request
    ) {
        return mapper.toResponse(commandService.end(
                new EndOrganizationalAssignmentCommand(
                        new OrganizationalAssignmentId(id),
                        request.effectiveTo()
                )
        ));
    }
}