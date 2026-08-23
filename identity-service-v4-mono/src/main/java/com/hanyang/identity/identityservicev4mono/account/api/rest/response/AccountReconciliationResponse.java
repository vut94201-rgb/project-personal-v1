package com.hanyang.identity.identityservicev4mono.account.api.rest.response;

import com.hanyang.identity.identityservicev4mono.account.application.provisioning.AccountReconciliationResult;

import java.util.List;
import java.util.UUID;

public record AccountReconciliationResponse(
        List<Result> results
) {
    public static AccountReconciliationResponse from(
            List<AccountReconciliationResult> reconciliationResults
    ) {
    return new AccountReconciliationResponse(
                                reconciliationResults.stream()
                                                .map(Result::from)
                                        .toList()
                       );
            }

         public record Result(
            UUID accountId,
            String provider,
            String status,
            String externalId,
            String externalCode,
            String error
    ) {
        private static Result from(AccountReconciliationResult result) {
    return new Result(
                                        result.accountId().value(),
                                        result.provider().name(),
                                        result.status().name(),
                                        result.externalId(),
                                        result.externalCode(),
                                      result.error()
                                        );
                  }
    }
}