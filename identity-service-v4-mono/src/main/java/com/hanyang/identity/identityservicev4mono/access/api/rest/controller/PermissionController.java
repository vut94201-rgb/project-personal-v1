package com.hanyang.identity.identityservicev4mono.access.api.rest.controller;


import com.hanyang.identity.identityservicev4mono.access.api.mapper.PermissionRestMapper;
import com.hanyang.identity.identityservicev4mono.access.api.rest.request.CreatePermissionRequest;
import com.hanyang.identity.identityservicev4mono.access.api.rest.request.UpdatePermissionRequest;
import com.hanyang.identity.identityservicev4mono.access.api.rest.response.PermissionResponse;
import com.hanyang.identity.identityservicev4mono.access.application.PermissionCommandService;
import com.hanyang.identity.identityservicev4mono.access.application.PermissionQueryService;
import com.hanyang.identity.identityservicev4mono.access.application.command.UpdatePermissionCommand;
import com.hanyang.identity.identityservicev4mono.access.domain.ApplicationId;
import com.hanyang.identity.identityservicev4mono.access.domain.Permission;
import com.hanyang.identity.identityservicev4mono.access.domain.PermissionId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionCommandService commandService;
    private final PermissionQueryService queryService;
    private final PermissionRestMapper mapper;

    @PostMapping
    public ResponseEntity<PermissionResponse> create(
            @Valid @RequestBody CreatePermissionRequest request
    ) {
        Permission permission = commandService.create(
                mapper.toCommand(request)
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mapper.toResponse(permission));
    }

    @GetMapping("/{id}")
    public PermissionResponse getById(@PathVariable UUID id) {
        return mapper.toResponse(
                queryService.getById(new PermissionId(id))
        );
    }

    @GetMapping
    public List<PermissionResponse> getByApplication(
            @RequestParam UUID applicationId
    ) {
        return queryService
                .getByApplicationId(new ApplicationId(applicationId))
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @PutMapping("/{id}")
    public PermissionResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePermissionRequest request
    ) {
        Permission permission = commandService.update(
                new UpdatePermissionCommand(
                        new PermissionId(id),
                        request.name()
                )
        );

        return mapper.toResponse(permission);
    }

    @PatchMapping("/{id}/disable")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disable(@PathVariable UUID id) {
        commandService.disable(new PermissionId(id));
    }
}