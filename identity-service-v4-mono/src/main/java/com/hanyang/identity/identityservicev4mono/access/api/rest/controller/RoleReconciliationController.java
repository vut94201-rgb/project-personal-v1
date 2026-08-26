package com.hanyang.identity.identityservicev4mono.access.api.rest.controller;

import com.hanyang.identity.identityservicev4mono.access.api.rest.request.ReconcileRolesRequest;
import com.hanyang.identity.identityservicev4mono.access.api.rest.response.RoleReconciliationResponse;
import com.hanyang.identity.identityservicev4mono.access.application.provisioning.RoleProvisioningService;
import com.hanyang.identity.identityservicev4mono.access.application.provisioning.RoleReconciliationResult;
import com.hanyang.identity.identityservicev4mono.access.domain.RoleId;
import com.hanyang.identity.identityservicev4mono.security.authorization.IdentityAdminAccess;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal/v1/identity-provider/roles")
@RequiredArgsConstructor
@IdentityAdminAccess
public class RoleReconciliationController {

  private final RoleProvisioningService provisioningService;

  @PostMapping("/reconcile")
  public RoleReconciliationResponse reconcile(@Valid @RequestBody ReconcileRolesRequest request) {
    List<RoleReconciliationResult> results =
        request.roleIds().stream()
            .distinct()
            .map(RoleId::new)
            .map(provisioningService::reconcile)
            .toList();

    return RoleReconciliationResponse.from(results);
  }
}
