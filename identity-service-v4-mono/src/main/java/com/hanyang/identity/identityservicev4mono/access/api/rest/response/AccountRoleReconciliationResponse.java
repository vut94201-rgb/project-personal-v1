package com.hanyang.identity.identityservicev4mono.access.api.rest.response;

import com.hanyang.identity.identityservicev4mono.access.application.provisioning.AccountRoleReconciliationResult;

import java.util.List;
import java.util.UUID;

public record AccountRoleReconciliationResponse(
        List<Result> results
) {    public static AccountRoleReconciliationResponse from(
            List<AccountRoleReconciliationResult> reconciliationResults
    ) {
            return new AccountRoleReconciliationResponse(
                                reconciliationResults.stream()
                                             .map(Result::from)
                                        .toList()
                       );
           }

            public record Result(
            UUID accountId,
            UUID roleId,            String provider,
            String status,
            boolean desiredAssigned,
            String error
    ) {
        private static Result from(AccountRoleReconciliationResult result) {
    return new Result(                    result.accountId().value(),
                                        result.roleId().value(),
                                        result.provider().name(),
                                        result.status().name(),
                                        result.desiredAssigned(),
                                      result.error()
                                        );
                    }
    }
}