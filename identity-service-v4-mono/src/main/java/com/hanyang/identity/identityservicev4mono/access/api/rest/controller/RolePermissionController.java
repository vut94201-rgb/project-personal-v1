package com.hanyang.identity.identityservicev4mono.access.api.rest.controller;


import com.hanyang.identity.identityservicev4mono.access.api.mapper.PermissionRestMapper;
import com.hanyang.identity.identityservicev4mono.access.api.rest.response.PermissionResponse;
import com.hanyang.identity.identityservicev4mono.access.application.RolePermissionCommandService;
import com.hanyang.identity.identityservicev4mono.access.application.RolePermissionQueryService;
import com.hanyang.identity.identityservicev4mono.access.domain.PermissionId;
import com.hanyang.identity.identityservicev4mono.access.domain.RoleId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/roles/{roleId}/permissions")
@RequiredArgsConstructor
public class RolePermissionController {

    private final RolePermissionCommandService commandService;
    private final RolePermissionQueryService queryService;
    private final PermissionRestMapper permissionMapper;

    @GetMapping
    public List<PermissionResponse> getPermissions(
            @PathVariable UUID roleId
    ) {
        return queryService
                .getPermissions(new RoleId(roleId))
                .stream()
                .map(permissionMapper::toResponse)
                .toList();
    }

    @PostMapping("/{permissionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void assign(
            @PathVariable UUID roleId,
            @PathVariable UUID permissionId
    ) {
        commandService.assign(
                new RoleId(roleId),
                new PermissionId(permissionId)
        );
    }

    @DeleteMapping("/{permissionId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revoke(
            @PathVariable UUID roleId,
            @PathVariable UUID permissionId
    ) {
        commandService.revoke(
                new RoleId(roleId),
                new PermissionId(permissionId)
        );
    }
}