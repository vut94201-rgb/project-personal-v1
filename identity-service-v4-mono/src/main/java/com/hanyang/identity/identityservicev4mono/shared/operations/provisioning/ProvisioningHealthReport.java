package com.hanyang.identity.identityservicev4mono.shared.operations.provisioning;


import java.time.Duration;
import java.time.Instant;
import java.util.List;

public record ProvisioningHealthReport(
        Instant checkedAt,
        String status,
        Duration staleAfter,
        ProviderHealth provider,
        List<ResourceHealth> resources
) {

    public record ProviderHealth(
            String provider,
            String status,
            String error
    ) {
    }

    public record ResourceHealth(
            String resource,
            long total,
            long pending,
            long syncing,
            long synced,
            long failed,
            long drifted,
            long staleUnsynced,
            Instant oldestUnsyncedAt
    ) {
    }
}