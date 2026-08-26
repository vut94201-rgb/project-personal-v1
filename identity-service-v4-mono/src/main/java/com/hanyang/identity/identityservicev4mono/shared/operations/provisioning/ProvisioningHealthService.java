package com.hanyang.identity.identityservicev4mono.shared.operations.provisioning;


import com.hanyang.identity.identityservicev4mono.infrastructure.keycloak.config.KeycloakProperties;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.time.Clock;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ProvisioningHealthService {

    private static final List<ResourceTable> RESOURCE_TABLES = List.of(
            new ResourceTable("APPLICATION", "application_identity_provider_bindings"),
            new ResourceTable("ROLE", "role_identity_provider_bindings"),
            new ResourceTable("ACCOUNT", "account_identity_provider_bindings"),
            new ResourceTable("ACCOUNT_DIRECTORY", "account_directory_bindings"),
            new ResourceTable("ACCOUNT_ROLE", "account_role_identity_provider_bindings")
    );

    private final JdbcTemplate jdbcTemplate;
    private final Keycloak keycloakAdminClient;
    private final KeycloakProperties keycloakProperties;
    private final ProvisioningHealthProperties properties;
    private final Clock clock;

    public ProvisioningHealthReport inspect() {
        Instant now = clock.instant();
        Instant staleBefore = now.minus(properties.getStaleAfter());

        List<ProvisioningHealthReport.ResourceHealth> resources = RESOURCE_TABLES.stream()
                .map(resource -> inspectResource(resource, staleBefore))
                .toList();

        ProvisioningHealthReport.ProviderHealth provider = inspectProvider();

        boolean degradedProvisioning = resources.stream().anyMatch(resource ->
                resource.failed() > 0
                        || resource.drifted() > 0
                        || resource.staleUnsynced() > 0
        );
        boolean degradedProvider = !"UP".equals(provider.status())
                && !"SKIPPED".equals(provider.status());

        return new ProvisioningHealthReport(
                now,
                degradedProvisioning || degradedProvider ? "DEGRADED" : "HEALTHY",
                properties.getStaleAfter(),
                provider,
                resources
        );
    }

    private ProvisioningHealthReport.ResourceHealth inspectResource(
            ResourceTable resource,
            Instant staleBefore
    ) {
        Map<String, Long> counts = new LinkedHashMap<>();

        jdbcTemplate.query(
                "SELECT sync_status, COUNT(*) FROM "
                        + resource.tableName()
                        + " GROUP BY sync_status",
                resultSet -> {
                    counts.put(
                            resultSet.getString(1),
                            resultSet.getLong(2)
                    );
                }
        );

        Long staleUnsynced = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM "
                        + resource.tableName()
                        + " WHERE sync_status <> 'SYNCED' AND updated_at <= ?",
                Long.class,
                Timestamp.from(staleBefore)
        );

        Instant oldestUnsyncedAt = jdbcTemplate.query(
                "SELECT MIN(updated_at) FROM "
                        + resource.tableName()
                        + " WHERE sync_status <> 'SYNCED'",
                resultSet -> {
                    if (!resultSet.next()) {
                        return null;
                    }
                    Timestamp timestamp = resultSet.getTimestamp(1);
                    return timestamp == null ? null : timestamp.toInstant();
                }
        );

        long pending = count(counts, "PENDING");
        long syncing = count(counts, "SYNCING");
        long synced = count(counts, "SYNCED");
        long failed = count(counts, "FAILED");
        long drifted = count(counts, "DRIFTED");

        return new ProvisioningHealthReport.ResourceHealth(
                resource.resourceName(),
                pending + syncing + synced + failed + drifted,
                pending,
                syncing,
                synced,
                failed,
                drifted,
                staleUnsynced == null ? 0L : staleUnsynced,
                oldestUnsyncedAt
        );
    }

    private ProvisioningHealthReport.ProviderHealth inspectProvider() {
        if (!properties.isProviderProbeEnabled()) {
            return new ProvisioningHealthReport.ProviderHealth(
                    "KEYCLOAK",
                    "SKIPPED",
                    null
            );
        }

        if (keycloakProperties.adminClientSecret() == null
                || keycloakProperties.adminClientSecret().isBlank()) {
            return new ProvisioningHealthReport.ProviderHealth(
                    "KEYCLOAK",
                    "NOT_CONFIGURED",
                    "integration.keycloak.admin-client-secret is blank"
            );
        }

        try {
            keycloakAdminClient
                    .realm(requireText(keycloakProperties.realm(), "integration.keycloak.realm"))
                    .toRepresentation();

            return new ProvisioningHealthReport.ProviderHealth(
                    "KEYCLOAK",
                    "UP",
                    null
            );
        } catch (ProcessingException exception) {
            return providerDown("Unable to connect to Keycloak Admin API");
        } catch (WebApplicationException exception) {
            return providerDown(
                    "Keycloak Admin API returned HTTP "
                            + exception.getResponse().getStatus()
            );
        } catch (RuntimeException exception) {
            String message = exception.getMessage();
            return providerDown(
                    message == null || message.isBlank()
                            ? exception.getClass().getSimpleName()
                            : message
            );
        }
    }

    private static ProvisioningHealthReport.ProviderHealth providerDown(String error) {
        return new ProvisioningHealthReport.ProviderHealth(
                "KEYCLOAK",
                "DOWN",
                error
        );
    }

    private static long count(Map<String, Long> counts, String status) {
        return counts.getOrDefault(status, 0L);
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(fieldName + " must not be blank");
        }
        return value.trim();
    }

    private record ResourceTable(
            String resourceName,
            String tableName
    ) {
    }
}