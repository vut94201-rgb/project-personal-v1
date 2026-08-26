package com.hanyang.identity.identityservicev4mono.account.api.rest.controller;


import com.hanyang.identity.identityservicev4mono.account.api.rest.request.ReconcileAccountsRequest;
import com.hanyang.identity.identityservicev4mono.account.api.rest.response.AccountReconciliationResponse;
import com.hanyang.identity.identityservicev4mono.account.application.provisioning.AccountProvisioningService;
import com.hanyang.identity.identityservicev4mono.account.application.provisioning.AccountReconciliationResult;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountId;
import com.hanyang.identity.identityservicev4mono.security.authorization.IdentityAdminAccess;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/internal/v1/identity-provider/accounts")
@RequiredArgsConstructor
@IdentityAdminAccess
public class AccountReconciliationController {

    private final AccountProvisioningService provisioningService;

    @PostMapping("/reconcile")
    public AccountReconciliationResponse reconcile(
            @Valid @RequestBody ReconcileAccountsRequest request
    ) {
        List<AccountReconciliationResult> results = request.accountIds().stream()
                .distinct()
                .map(AccountId::new)
                .map(provisioningService::reconcile)
                .toList();

        return AccountReconciliationResponse.from(results);
    }
}