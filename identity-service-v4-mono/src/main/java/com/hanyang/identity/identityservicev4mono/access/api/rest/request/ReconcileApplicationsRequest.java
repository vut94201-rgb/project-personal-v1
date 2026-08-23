package com.hanyang.identity.identityservicev4mono.access.api.rest.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record ReconcileApplicationsRequest(
        @NotEmpty
        @Size(max = 100)
        List<@NotNull UUID> applicationIds
) {
        }