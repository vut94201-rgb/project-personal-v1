package com.hanyang.identity.identityservicev4mono.access.api.rest.controller;

import com.hanyang.identity.identityservicev4mono.access.api.rest.response.ServicePrincipalRoleResponse;
import com.hanyang.identity.identityservicev4mono.access.application.ServicePrincipalRoleCommandService;
import com.hanyang.identity.identityservicev4mono.access.application.ServicePrincipalRoleQueryService;
import com.hanyang.identity.identityservicev4mono.access.application.provisioning.ServicePrincipalRoleProvisioningState;
import com.hanyang.identity.identityservicev4mono.access.domain.Role;
import com.hanyang.identity.identityservicev4mono.access.domain.RoleId;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/service-principals/{servicePrincipalId}/roles")
@RequiredArgsConstructor
public class ServicePrincipalRoleController {

    private final ServicePrincipalRoleCommandService commandService;
    private final ServicePrincipalRoleQueryService queryService;

    @GetMapping
    public List<ServicePrincipalRoleResponse> list(
            @PathVariable UUID servicePrincipalId
    ) {
        return queryService.list(new ServicePrincipalId(servicePrincipalId)).stream()
                .map(view -> toResponse(view.role(), view.provisioning()))
                .toList();
    }

    @PutMapping("/{roleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void assign(
            @PathVariable UUID servicePrincipalId,
            @PathVariable UUID roleId
    ) {
        commandService.assign(
                new ServicePrincipalId(servicePrincipalId),
                new RoleId(roleId)
        );
    }

    @DeleteMapping("/{roleId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revoke(
            @PathVariable UUID servicePrincipalId,
            @PathVariable UUID roleId
    ) {
        commandService.revoke(
                new ServicePrincipalId(servicePrincipalId),
                new RoleId(roleId)
        );
    }

    private static ServicePrincipalRoleResponse toResponse(
            Role role,
            ServicePrincipalRoleProvisioningState provisioning
    ) {
        return new ServicePrincipalRoleResponse(
                role.getId().value(),
                role.getApplicationId().value(),
                role.getCode(),
                role.getName(),
                role.getStatus(),
                provisioning == null ? null : provisioning.getProvider(),
                provisioning == null ? null : provisioning.getStatus(),
                provisioning == null ? 0L : provisioning.getDesiredRevision(),
                provisioning == null ? 0L : provisioning.getSyncedRevision(),
                provisioning == null ? null : provisioning.getLastSyncedAt(),
                provisioning == null ? null : provisioning.getLastError()
        );
    }
}