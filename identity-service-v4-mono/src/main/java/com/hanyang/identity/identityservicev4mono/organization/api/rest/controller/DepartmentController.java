package com.hanyang.identity.identityservicev4mono.organization.api.rest.controller;

import com.hanyang.identity.identityservicev4mono.organization.api.mapper.DepartmentRestMapper;
import com.hanyang.identity.identityservicev4mono.organization.api.rest.request.CreateDepartmentRequest;
import com.hanyang.identity.identityservicev4mono.organization.api.rest.request.UpdateDepartmentRequest;
import com.hanyang.identity.identityservicev4mono.organization.api.rest.response.DepartmentResponse;
import com.hanyang.identity.identityservicev4mono.organization.application.DepartmentCommandService;
import com.hanyang.identity.identityservicev4mono.organization.application.DepartmentQueryService;
import com.hanyang.identity.identityservicev4mono.organization.application.command.UpdateDepartmentCommand;
import com.hanyang.identity.identityservicev4mono.organization.domain.DepartmentId;
import com.hanyang.identity.identityservicev4mono.organization.domain.OrganizationReferenceStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/departments")
@RequiredArgsConstructor
public class DepartmentController {
    private final DepartmentCommandService commandService;
    private final DepartmentQueryService queryService;
    private final DepartmentRestMapper mapper;

    @PostMapping
    public ResponseEntity<DepartmentResponse> create(@Valid @RequestBody CreateDepartmentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toResponse(commandService.create(mapper.toCommand(request))));
    }

    @GetMapping("/{id}")
    public DepartmentResponse getById(@PathVariable UUID id) {
        return mapper.toResponse(queryService.getById(new DepartmentId(id)));
    }

    @GetMapping("/by-code/{code}")
    public DepartmentResponse getByCode(@PathVariable String code) {
        return mapper.toResponse(queryService.getByCode(code));
    }

    @GetMapping
    public List<DepartmentResponse> findAll(@RequestParam(required = false) String status) {
        OrganizationReferenceStatus parsed = OrganizationReferenceStatus.fromExternalValue(status);
        return queryService.findAll(parsed).stream().map(mapper::toResponse).toList();
    }

    @PutMapping("/{id}")
    public DepartmentResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateDepartmentRequest request
    ) {
        return mapper.toResponse(commandService.update(
                new UpdateDepartmentCommand(new DepartmentId(id), request.name())
        ));
    }

    @PatchMapping("/{id}/disable")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disable(@PathVariable UUID id) {
        commandService.disable(new DepartmentId(id));
    }
}
