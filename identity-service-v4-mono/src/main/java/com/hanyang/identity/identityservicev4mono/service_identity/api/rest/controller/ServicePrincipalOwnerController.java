package com.hanyang.identity.identityservicev4mono.service_identity.api.rest.controller;

import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import com.hanyang.identity.identityservicev4mono.service_identity.api.mapper.ServicePrincipalRestMapper;
import com.hanyang.identity.identityservicev4mono.service_identity.api.rest.request.AssignServicePrincipalOwnerRequest;
import com.hanyang.identity.identityservicev4mono.service_identity.api.rest.request.TransferServicePrincipalPrimaryOwnerRequest;
import com.hanyang.identity.identityservicev4mono.service_identity.api.rest.response.ServicePrincipalOwnerResponse;
import com.hanyang.identity.identityservicev4mono.service_identity.application.ServicePrincipalOwnershipCommandService;
import com.hanyang.identity.identityservicev4mono.service_identity.application.command.AssignServicePrincipalOwnerCommand;
import com.hanyang.identity.identityservicev4mono.service_identity.application.command.RevokeServicePrincipalOwnerCommand;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalId;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalOwnerId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class ServicePrincipalOwnerController {

    private final ServicePrincipalOwnershipCommandService ownershipCommandService;
    private final ServicePrincipalRestMapper mapper;

    @PostMapping("/api/v1/service-principals/{servicePrincipalId}/owners")
    public ResponseEntity<ServicePrincipalOwnerResponse> assign(
            @PathVariable UUID servicePrincipalId,
            @Valid @RequestBody AssignServicePrincipalOwnerRequest request
    ) {
        var owner = ownershipCommandService.assign(
                new AssignServicePrincipalOwnerCommand(
                        new ServicePrincipalId(servicePrincipalId),
                        new EmployeeId(request.employeeId()),
                        request.ownershipType()
                )
        );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mapper.toOwnerResponse(owner));
    }



    @PutMapping("/api/v1/service-principals/{servicePrincipalId}/owners/primary")
    public ServicePrincipalOwnerResponse transferPrimary(
            @PathVariable UUID servicePrincipalId,
            @Valid @RequestBody TransferServicePrincipalPrimaryOwnerRequest request
    ) {
        return mapper.toOwnerResponse(
                ownershipCommandService.transferPrimaryOwner(
                        new ServicePrincipalId(servicePrincipalId),
                        new EmployeeId(request.employeeId())
                )
        );
    }

    @PatchMapping("/api/v1/service-principal-owners/{ownerId}/revoke")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void revoke(@PathVariable UUID ownerId) {
        ownershipCommandService.revoke(
                new RevokeServicePrincipalOwnerCommand(
                        new ServicePrincipalOwnerId(ownerId)
                )
        );
    }
}