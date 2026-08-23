package com.hanyang.identity.identityservicev4mono.access.api.rest.controller;

import com.hanyang.identity.identityservicev4mono.access.api.rest.request.ReconcileAccountRolesRequest;
import com.hanyang.identity.identityservicev4mono.access.api.rest.response.AccountRoleReconciliationResponse;
import com.hanyang.identity.identityservicev4mono.access.application.provisioning.AccountRoleProvisioningService;
import com.hanyang.identity.identityservicev4mono.access.application.provisioning.AccountRoleReconciliationResult;
import com.hanyang.identity.identityservicev4mono.access.domain.RoleId;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal/v1/identity-provider/account-roles")
@RequiredArgsConstructor
public class AccountRoleReconciliationController {

    private final AccountRoleProvisioningService provisioningService;

    @PostMapping("/reconcile")
    public AccountRoleReconciliationResponse reconcile(
            @Valid @RequestBody ReconcileAccountRolesRequest request
    ) {
        List<AccountRoleReconciliationResult> results = request.assignments().stream()
                .distinct()
                .map(entry -> provisioningService.reconcile(
                        new AccountId(entry.accountId()),
                        new RoleId(entry.roleId())
                ))
                .toList();

        return AccountRoleReconciliationResponse.from(results);
    }
}