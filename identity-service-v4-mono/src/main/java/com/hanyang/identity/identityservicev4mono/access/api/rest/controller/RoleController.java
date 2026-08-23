package com.hanyang.identity.identityservicev4mono.access.api.rest.controller;


import com.hanyang.identity.identityservicev4mono.access.api.mapper.RoleRestMapper;
import com.hanyang.identity.identityservicev4mono.access.api.rest.request.CreateRoleRequest;
import com.hanyang.identity.identityservicev4mono.access.api.rest.request.UpdateRoleRequest;
import com.hanyang.identity.identityservicev4mono.access.api.rest.response.RoleResponse;
import com.hanyang.identity.identityservicev4mono.access.application.RoleCommandService;
import com.hanyang.identity.identityservicev4mono.access.application.RoleQueryService;
import com.hanyang.identity.identityservicev4mono.access.application.command.UpdateRoleCommand;
import com.hanyang.identity.identityservicev4mono.access.domain.ApplicationId;
import com.hanyang.identity.identityservicev4mono.access.domain.Role;
import com.hanyang.identity.identityservicev4mono.access.domain.RoleId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleCommandService commandService;
    private final RoleQueryService queryService;
    private final RoleRestMapper mapper;

    @PostMapping
    public ResponseEntity<RoleResponse> create(
            @Valid @RequestBody CreateRoleRequest request
    ) {
        Role role = commandService.create(
                mapper.toCommand(request)
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mapper.toResponse(role));
    }

    @GetMapping("/{id}")
    public RoleResponse getById(@PathVariable UUID id) {
        return mapper.toResponse(
                queryService.getById(new RoleId(id))
        );
    }

    @GetMapping
    public List<RoleResponse> getByApplication(
            @RequestParam UUID applicationId
    ) {
        return queryService
                .getByApplicationId(new ApplicationId(applicationId))
                .stream()
                .map(mapper::toResponse)
                .toList();
    }

    @PutMapping("/{id}")
    public RoleResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateRoleRequest request
    ) {
        Role role = commandService.update(
                new UpdateRoleCommand(
                        new RoleId(id),
                        request.name()
                )
        );

        return mapper.toResponse(role);
    }

    @PatchMapping("/{id}/disable")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disable(@PathVariable UUID id) {
        commandService.disable(new RoleId(id));
    }
}