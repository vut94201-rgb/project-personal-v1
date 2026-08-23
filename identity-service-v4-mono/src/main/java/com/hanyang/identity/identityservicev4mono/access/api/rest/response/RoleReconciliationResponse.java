package com.hanyang.identity.identityservicev4mono.access.api.rest.response;

import com.hanyang.identity.identityservicev4mono.access.application.provisioning.RoleReconciliationResult;

import java.util.List;
import java.util.UUID;

public record RoleReconciliationResponse(
        List<Result> results
) {
    public static RoleReconciliationResponse from(
            List<RoleReconciliationResult> reconciliationResults
    ) {
           return new RoleReconciliationResponse(
                               reconciliationResults.stream()
                                                .map(Result::from)
                                        .toList()
                       );
           }

    public record Result(
            UUID roleId,
            String provider,
            String status,
            String externalId,
            String externalCode,
            String error
    ) {
      private static Result from(RoleReconciliationResult result) {
                    return new Result(
                                        result.roleId().value(),
                                        result.provider().name(),
                                        result.status().name(),
                                        result.externalId(),
                                        result.externalCode(),
                    result.error()
                                        );
                   }
    }
}