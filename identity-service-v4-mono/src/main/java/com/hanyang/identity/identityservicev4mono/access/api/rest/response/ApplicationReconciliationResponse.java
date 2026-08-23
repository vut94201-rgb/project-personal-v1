package com.hanyang.identity.identityservicev4mono.access.api.rest.response;

import com.hanyang.identity.identityservicev4mono.access.application.provisioning.ApplicationReconciliationResult;

import java.util.List;
import java.util.UUID;

public record ApplicationReconciliationResponse(
        List<Result> results
) {
    public static ApplicationReconciliationResponse from(
            List<ApplicationReconciliationResult> reconciliationResults
    ) {
        return new ApplicationReconciliationResponse(
                reconciliationResults.stream()
                        .map(Result::from)
                        .toList()
        );
    }

    public record Result(
            UUID applicationId,
            String provider,
            String status,
            String externalId,
            String externalCode,
            String error
    ) {
        private static Result from(ApplicationReconciliationResult result) {
            return new Result(
                    result.applicationId().value(),
                    result.provider().name(),
                    result.status().name(),
                    result.externalId(),
                    result.externalCode(),
                    result.error()
            );
        }
    }
}