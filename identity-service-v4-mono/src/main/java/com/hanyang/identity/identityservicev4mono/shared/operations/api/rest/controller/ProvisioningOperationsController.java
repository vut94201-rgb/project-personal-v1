package com.hanyang.identity.identityservicev4mono.shared.operations.api.rest.controller;

import com.hanyang.identity.identityservicev4mono.security.authorization.IdentityAdminAccess;
import com.hanyang.identity.identityservicev4mono.shared.operations.provisioning.ProvisioningHealthReport;
import com.hanyang.identity.identityservicev4mono.shared.operations.provisioning.ProvisioningHealthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/v1/operations")
@RequiredArgsConstructor
@IdentityAdminAccess
public class ProvisioningOperationsController {

            private final ProvisioningHealthService provisioningHealthService;

            @GetMapping("/provisioning-health")
    public ProvisioningHealthReport provisioningHealth() {
                return provisioningHealthService.inspect();
           }
}