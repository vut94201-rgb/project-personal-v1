package com.hanyang.identity.identityservicev4mono.service_identity.api.rest.request;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record TransferServicePrincipalPrimaryOwnerRequest(
        @NotNull UUID employeeId
) {
}