package com.hanyang.identity.identityservicev4mono.access.api.rest.controller;

import com.hanyang.identity.identityservicev4mono.access.api.rest.request.ReconcileApplicationsRequest;
import com.hanyang.identity.identityservicev4mono.access.api.rest.response.ApplicationReconciliationResponse;
import com.hanyang.identity.identityservicev4mono.access.application.provisioning.ApplicationProvisioningService;
import com.hanyang.identity.identityservicev4mono.access.application.provisioning.ApplicationReconciliationResult;
import com.hanyang.identity.identityservicev4mono.access.domain.ApplicationId;
import com.hanyang.identity.identityservicev4mono.security.authorization.IdentityAdminAccess;
import com.hanyang.identity.identityservicev4mono.security.authorization.IdentityReadAccess;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal/v1/identity-provider/applications")
@RequiredArgsConstructor
@IdentityAdminAccess
public class ApplicationReconciliationController {

  private final ApplicationProvisioningService provisioningService;

  @PostMapping("/reconcile")
  public ApplicationReconciliationResponse reconcile(
      @Valid @RequestBody ReconcileApplicationsRequest request) {
    List<ApplicationReconciliationResult> results =
        request.applicationIds().stream()
            .distinct()
            .map(ApplicationId::new)
            .map(provisioningService::reconcile)
            .toList();

    return ApplicationReconciliationResponse.from(results);
  }
}
