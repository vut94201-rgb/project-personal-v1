package com.hanyang.identity.identityservicev4mono.organization.api.rest.controller;

import com.hanyang.identity.identityservicev4mono.organization.api.mapper.CrewRestMapper;
import com.hanyang.identity.identityservicev4mono.organization.api.rest.request.CreateCrewRequest;
import com.hanyang.identity.identityservicev4mono.organization.api.rest.request.UpdateCrewRequest;
import com.hanyang.identity.identityservicev4mono.organization.api.rest.response.CrewResponse;
import com.hanyang.identity.identityservicev4mono.organization.application.CrewCommandService;
import com.hanyang.identity.identityservicev4mono.organization.application.CrewQueryService;
import com.hanyang.identity.identityservicev4mono.organization.application.command.UpdateCrewCommand;
import com.hanyang.identity.identityservicev4mono.organization.domain.CrewId;
import com.hanyang.identity.identityservicev4mono.organization.domain.DepartmentId;
import com.hanyang.identity.identityservicev4mono.organization.domain.OrganizationReferenceStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/crews")
@RequiredArgsConstructor
public class CrewController {
    private final CrewCommandService commandService;
    private final CrewQueryService queryService;
    private final CrewRestMapper mapper;

    @PostMapping
    public ResponseEntity<CrewResponse> create(@Valid @RequestBody CreateCrewRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toResponse(commandService.create(mapper.toCommand(request))));
    }

    @GetMapping("/{id}")
    public CrewResponse getById(@PathVariable UUID id) {
        return mapper.toResponse(queryService.getById(new CrewId(id)));
    }

    @GetMapping
    public List<CrewResponse> findByDepartment(
            @RequestParam UUID departmentId,
            @RequestParam(required = false) String status
    ) {
        OrganizationReferenceStatus parsed = OrganizationReferenceStatus.fromExternalValue(status);
        return queryService.findByDepartment(new DepartmentId(departmentId), parsed)
                .stream().map(mapper::toResponse).toList();
    }

    @PutMapping("/{id}")
    public CrewResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateCrewRequest request
    ) {
        return mapper.toResponse(commandService.update(
                new UpdateCrewCommand(new CrewId(id), request.name())
        ));
    }

    @PatchMapping("/{id}/disable")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disable(@PathVariable UUID id) {
        commandService.disable(new CrewId(id));
    }
}