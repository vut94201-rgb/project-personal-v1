package com.hanyang.identity.identityservicev4mono.service_identity.api.rest.controller;

import com.hanyang.identity.identityservicev4mono.security.authorization.IdentityAdminAccess;
import com.hanyang.identity.identityservicev4mono.service_identity.api.mapper.ServicePrincipalRestMapper;
import com.hanyang.identity.identityservicev4mono.service_identity.api.rest.request.CreateServicePrincipalRequest;
import com.hanyang.identity.identityservicev4mono.service_identity.api.rest.request.UpdateServicePrincipalRequest;
import com.hanyang.identity.identityservicev4mono.service_identity.api.rest.response.ServicePrincipalResponse;
import com.hanyang.identity.identityservicev4mono.service_identity.application.ServicePrincipalCommandService;
import com.hanyang.identity.identityservicev4mono.service_identity.application.ServicePrincipalLifecycleService;
import com.hanyang.identity.identityservicev4mono.service_identity.application.ServicePrincipalQueryService;
import com.hanyang.identity.identityservicev4mono.service_identity.application.provisioning.ServicePrincipalProvisioningService;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalId;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/service-principals")
@RequiredArgsConstructor
public class ServicePrincipalController {

    private final ServicePrincipalCommandService commandService;
    private final ServicePrincipalLifecycleService lifecycleService;
    private final ServicePrincipalQueryService queryService;
    private final ServicePrincipalProvisioningService provisioningService;
    private final ServicePrincipalRestMapper mapper;

    @PostMapping
    public ResponseEntity<ServicePrincipalResponse> create(
            @Valid @RequestBody CreateServicePrincipalRequest request
    ) {
        var created = commandService.create(mapper.toCommand(request));

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mapper.toResponse(queryService.getById(created.getId())));
    }

    @GetMapping("/{id}")
    public ServicePrincipalResponse getById(@PathVariable UUID id) {
        return mapper.toResponse(
                queryService.getById(new ServicePrincipalId(id))
        );
    }

    @GetMapping("/by-code/{code}")
    public ServicePrincipalResponse getByCode(@PathVariable String code) {
        return mapper.toResponse(queryService.getByCode(code));
    }

    @GetMapping
    public List<ServicePrincipalResponse> list(
            @RequestParam(required = false) ServicePrincipalStatus status
    ) {
        return queryService.list(status).stream()
                .map(mapper::toResponse)
                .toList();
    }

    @PutMapping("/{id}")
    public ServicePrincipalResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateServicePrincipalRequest request
    ) {
        ServicePrincipalId servicePrincipalId = new ServicePrincipalId(id);
        commandService.update(mapper.toCommand(servicePrincipalId, request));
        return mapper.toResponse(queryService.getById(servicePrincipalId));
    }

    @PatchMapping("/{id}/disable")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disable(@PathVariable UUID id) {
        lifecycleService.disable(new ServicePrincipalId(id));
    }

    @PostMapping("/{id}/reconcile")
    @ResponseStatus(HttpStatus.ACCEPTED)
    @IdentityAdminAccess
    public void requestReconciliation(@PathVariable UUID id) {
        provisioningService.requestSynchronization(new ServicePrincipalId(id));
    }
}