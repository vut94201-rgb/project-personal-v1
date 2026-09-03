package com.hanyang.identity.identityservicev4mono.account.api.rest.controller;

import com.hanyang.identity.identityservicev4mono.account.application.directory.provisioning.AccountDirectoryProvisioningService;
import com.hanyang.identity.identityservicev4mono.account.application.directory.provisioning.AccountDirectoryReconciliationResult;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountId;
import com.hanyang.identity.identityservicev4mono.security.authorization.IdentityAdminAccess;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/internal/v1/directory/accounts")
@RequiredArgsConstructor
@IdentityAdminAccess
public class AccountDirectoryReconciliationController {

    private final AccountDirectoryProvisioningService provisioningService;

    @PostMapping("/{accountId}/reconcile")
    public AccountDirectoryReconciliationResult reconcile(
            @PathVariable UUID accountId
    ) {
        return provisioningService.reconcile(
                new AccountId(accountId)
        );
    }
}