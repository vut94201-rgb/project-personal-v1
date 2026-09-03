package com.hanyang.identity.identityservicev4mono.account.api.rest.controller;

import com.hanyang.identity.identityservicev4mono.account.api.rest.response.InitialPasswordResponse;

import com.hanyang.identity.identityservicev4mono.account.application.credential.AccountCredentialOnboardingService;
import com.hanyang.identity.identityservicev4mono.account.application.credential.InitialPasswordOnboardingResult;

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

  @PostMapping("/initial-password")
  public ResponseEntity<InitialPasswordResponse> issueInitialPassword(
      @PathVariable UUID accountId) {
    InitialPasswordOnboardingResult result =
        onboardingService.issueInitialPassword(new AccountId(accountId));

    return ResponseEntity.ok()
        .cacheControl(CacheControl.noStore())
        .header("Pragma", "no-cache")
        .body(new InitialPasswordResponse(result.initialPassword()));
  }
}
