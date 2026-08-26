package com.hanyang.identity.identityservicev4mono.account.api.rest.controller;


import com.hanyang.identity.identityservicev4mono.account.api.rest.response.TemporaryPasswordResponse;
import com.hanyang.identity.identityservicev4mono.account.application.credential.AccountCredentialOnboardingService;
import com.hanyang.identity.identityservicev4mono.account.application.credential.TemporaryPasswordOnboardingResult;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountId;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts/{accountId}/credential-onboarding")
@RequiredArgsConstructor
public class AccountCredentialOnboardingController {

    private final AccountCredentialOnboardingService onboardingService;

    @PostMapping("/temporary-password")
    public ResponseEntity<TemporaryPasswordResponse> issueTemporaryPassword(
            @PathVariable UUID accountId
    ) {
        TemporaryPasswordOnboardingResult result =
                onboardingService.issueTemporaryPassword(
                        new AccountId(accountId)
                );

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .header("Pragma", "no-cache")
                .body(new TemporaryPasswordResponse(
                        result.temporaryPassword()
                ));
    }

    @PostMapping("/password-setup-email")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sendPasswordSetupEmail(
            @PathVariable UUID accountId
    ) {
        onboardingService.sendPasswordSetupEmail(
                new AccountId(accountId)
        );
    }
}