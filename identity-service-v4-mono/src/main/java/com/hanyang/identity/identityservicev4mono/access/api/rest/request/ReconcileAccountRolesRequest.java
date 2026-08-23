package com.hanyang.identity.identityservicev4mono.access.api.rest.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;
import java.util.UUID;

public record ReconcileAccountRolesRequest(
        @NotEmpty
        @Size(max = 100)
        List<@Valid @NotNull Entry> assignments
) {
            public record Entry(
            @NotNull UUID accountId,
            @NotNull UUID roleId
    ) {
    }
}