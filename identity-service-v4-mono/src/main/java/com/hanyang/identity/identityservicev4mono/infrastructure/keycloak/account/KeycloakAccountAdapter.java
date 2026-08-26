package com.hanyang.identity.identityservicev4mono.infrastructure.keycloak.account;

import com.hanyang.identity.identityservicev4mono.account.application.port.IdentityProviderAccountPort;
import com.hanyang.identity.identityservicev4mono.infrastructure.keycloak.config.KeycloakProperties;
import com.hanyang.identity.identityservicev4mono.infrastructure.keycloak.exception.KeycloakIntegrationException;
import com.hanyang.identity.identityservicev4mono.infrastructure.keycloak.exception.KeycloakUserConflictException;
import com.hanyang.identity.identityservicev4mono.infrastructure.keycloak.federation.KeycloakLdapFederationManager;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class KeycloakAccountAdapter
        implements IdentityProviderAccountPort {

    private final Keycloak keycloakAdminClient;
    private final KeycloakProperties properties;
    private final KeycloakLdapFederationManager federationManager;

    @Override
    public ProvisionedAccount ensureAccount(
            String username,
            String externalId,
            boolean enabled
    ) {
        String normalizedUsername = requireText(username, "username");
        String normalizedExternalId = normalizeNullable(externalId);

        try {
            String federationProviderId = federationManager.requireProviderId();
            UserRepresentation user = requireFederatedUser(
                    normalizedUsername,
                    normalizedExternalId,
                    federationProviderId
            );

            UserResource userResource = users().get(
                    requireText(user.getId(), "Keycloak user id")
            );
            user = userResource.toRepresentation();
            requireExpectedFederation(user, federationProviderId, normalizedUsername);

            // Username is directory-owned. Do not attempt to rename a
            // federated LDAP identity from Keycloak.
            if (!Objects.equals(user.isEnabled(), enabled)) {
                user.setEnabled(enabled);
                userResource.update(user);
            }

            return new ProvisionedAccount(
                    requireText(user.getId(), "Keycloak user id"),
                    normalizedUsername
            );
        } catch (ProcessingException exception) {
            throw new KeycloakIntegrationException(
                    "Unable to connect to Keycloak Admin API",
                    exception
            );
        } catch (WebApplicationException exception) {
            throw new KeycloakIntegrationException(
                    "Unable to synchronize federated Keycloak user. HTTP "
                            + exception.getResponse().getStatus(),
                    exception
            );
        } catch (RuntimeException exception) {
            if (exception instanceof KeycloakIntegrationException integrationException) {
                throw integrationException;
            }
            throw new KeycloakIntegrationException(
                    "Unable to synchronize federated Keycloak user",
                    exception
            );
        }
    }

    @Override
    public ProvisionedAccount disableAccount(
            String username,
            String externalId
    ) {
        String normalizedUsername = requireText(username, "username");
        String normalizedExternalId = normalizeNullable(externalId);

        try {
            String federationProviderId = federationManager.requireProviderId();
            UserRepresentation user = resolveFederatedUser(
                    normalizedUsername,
                    normalizedExternalId,
                    federationProviderId,
                    false
            );

            // A PENDING/DISABLED account may have never been imported into
            // Keycloak. Directory lock is already authoritative for bind
            // denial, so absence in Keycloak is safe and idempotent here.
            if (user == null) {
                return new ProvisionedAccount(null, normalizedUsername);
            }

            UserResource userResource = users().get(
                    requireText(user.getId(), "Keycloak user id")
            );
            user = userResource.toRepresentation();
            requireExpectedFederation(user, federationProviderId, normalizedUsername);

            if (!Boolean.FALSE.equals(user.isEnabled())) {
                user.setEnabled(false);
                userResource.update(user);
            }

            // Disabling future authentication is not enough: the user may
            // already own active SSO/refresh sessions. Keycloak's per-user
            // logout endpoint removes those sessions and notifies clients
            // with admin URLs so the DISABLED business state is enforced
            // against already-authenticated users as well.
            userResource.logout();

            return new ProvisionedAccount(
                    requireText(user.getId(), "Keycloak user id"),
                    normalizedUsername
            );
        } catch (ProcessingException exception) {
            throw new KeycloakIntegrationException(
                    "Unable to connect to Keycloak Admin API",
                    exception
            );
        } catch (WebApplicationException exception) {
            throw new KeycloakIntegrationException(
                    "Unable to disable federated Keycloak user. HTTP "
                            + exception.getResponse().getStatus(),
                    exception
            );
        } catch (RuntimeException exception) {
            if (exception instanceof KeycloakIntegrationException integrationException) {
                throw integrationException;
            }
            throw new KeycloakIntegrationException(
                    "Unable to disable federated Keycloak user",
                    exception
            );
        }
    }

    private UserRepresentation requireFederatedUser(
            String username,
            String externalId,
            String federationProviderId
    ) {
        UserRepresentation user = resolveFederatedUser(
                username,
                externalId,
                federationProviderId,
                true
        );
        if (user == null) {
            throw new KeycloakIntegrationException(
                    "Federated Keycloak user not found for username: " + username
                            + ". Ensure 389 DS directory provisioning is SYNCED before Keycloak reconciliation."
            );
        }
        return user;
    }

    private UserRepresentation resolveFederatedUser(
            String username,
            String externalId,
            String federationProviderId,
            boolean failOnLocalConflict
    ) {
        if (externalId != null) {
            try {
                UserRepresentation byId = users().get(externalId).toRepresentation();
                if (belongsToFederation(byId, federationProviderId)) {
                    return byId;
                }
                // A legacy local Keycloak id from the pre-LDAP architecture
                // must not be treated as the new external binding.
            } catch (NotFoundException ignored) {
                // Keycloak may have been restored and generated new internal
                // ids. Fall back to stable username and repair the binding.
            }
        }

        List<UserRepresentation> matches = users().searchByUsername(username, true);
        if (matches == null || matches.isEmpty()) {
            return null;
        }

        UserRepresentation federated = matches.stream()
                .filter(user -> Objects.equals(user.getUsername(), username))
                .filter(user -> belongsToFederation(user, federationProviderId))
                .findFirst()
                .orElse(null);
        if (federated != null) {
            return federated;
        }

        boolean localConflict = matches.stream()
                .filter(user -> Objects.equals(user.getUsername(), username))
                .anyMatch(user -> normalizeNullable(user.getFederationLink()) == null);

        if (localConflict && failOnLocalConflict) {
            throw new KeycloakIntegrationException(
                    "Legacy local Keycloak user conflicts with LDAP-federated username: "
                            + username
                            + ". Migrate or remove the local user before federation can bind this account."
            );
        }

        return null;
    }

    private static void requireExpectedFederation(
            UserRepresentation user,
            String federationProviderId,
            String username
    ) {
        if (!belongsToFederation(user, federationProviderId)) {
            throw new KeycloakIntegrationException(
                    "Keycloak user is not linked to the configured LDAP federation: " + username
            );
        }
    }

    private static boolean belongsToFederation(
            UserRepresentation user,
            String federationProviderId
    ) {
        return user != null
                && Objects.equals(
                normalizeNullable(user.getFederationLink()),
                federationProviderId
        );
    }

    private UsersResource users() {
        return keycloakAdminClient
                .realm(requireText(properties.realm(), "integration.keycloak.realm"))
                .users();
    }

    private static String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }

    private static String requireText(
            String value,
            String fieldName
    ) {
        if (value == null || value.isBlank()) {
            throw new KeycloakIntegrationException(fieldName + " must not be blank");
        }
        return value.trim();
    }
}