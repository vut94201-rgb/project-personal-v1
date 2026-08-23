package com.hanyang.identity.identityservicev4mono.infrastructure.keycloak.account;

import com.hanyang.identity.identityservicev4mono.account.application.port.IdentityProviderAccountPort;
import com.hanyang.identity.identityservicev4mono.infrastructure.keycloak.config.KeycloakProperties;
import com.hanyang.identity.identityservicev4mono.infrastructure.keycloak.exception.KeycloakIntegrationException;
import com.hanyang.identity.identityservicev4mono.infrastructure.keycloak.exception.KeycloakUserConflictException;
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

    private static final int HTTP_CREATED = 201;
    private static final int HTTP_CONFLICT = 409;

    private final Keycloak keycloakAdminClient;
    private final KeycloakProperties properties;

    @Override
    public ProvisionedAccount ensureAccount(
            String username,
            String externalId,
            boolean enabled
    ) {
        String normalizedUsername = requireText(username, "username");
        String normalizedExternalId = normalizeNullable(externalId);

        try {
            UserRepresentation user = resolveExisting(
                    normalizedUsername,
                    normalizedExternalId
            );

            if (user == null) {
                user = createUser(normalizedUsername, enabled);
            }

            UserResource userResource = users().get(
                    requireText(user.getId(), "Keycloak user id")
            );
            user = userResource.toRepresentation();

            boolean changed = false;
            if (!Objects.equals(user.getUsername(), normalizedUsername)) {
                user.setUsername(normalizedUsername);
                changed = true;
            }
            if (!Objects.equals(user.isEnabled(), enabled)) {
                user.setEnabled(enabled);
                changed = true;
            }

            if (changed) {
                userResource.update(user);
            }

            return new ProvisionedAccount(
                    requireText(user.getId(), "Keycloak user id"),
                    normalizedUsername
            );
        } catch (KeycloakUserConflictException exception) {
            throw exception;
        } catch (ProcessingException exception) {
            throw new KeycloakIntegrationException(
                    "Unable to connect to Keycloak Admin API",
                    exception
            );
        } catch (WebApplicationException exception) {
            throw new KeycloakIntegrationException(
                    "Unable to synchronize Keycloak user. HTTP "
                            + exception.getResponse().getStatus(),
                    exception
            );
        } catch (RuntimeException exception) {
            if (exception instanceof KeycloakIntegrationException integrationException) {
                throw integrationException;
            }
            throw new KeycloakIntegrationException(
                    "Unable to synchronize Keycloak user",
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
            UserRepresentation user = resolveExisting(
                    normalizedUsername,
                    normalizedExternalId
            );

            if (user == null) {
                return new ProvisionedAccount(null, normalizedUsername);
            }

            UserResource userResource = users().get(
                    requireText(user.getId(), "Keycloak user id")
            );
            user = userResource.toRepresentation();

            boolean changed = false;
            if (!Objects.equals(user.getUsername(), normalizedUsername)) {
                user.setUsername(normalizedUsername);
                changed = true;
            }
            if (!Boolean.FALSE.equals(user.isEnabled())) {
                user.setEnabled(false);
                changed = true;
            }

            if (changed) {
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
                    "Unable to disable Keycloak user. HTTP "
                            + exception.getResponse().getStatus(),
                    exception
            );
        } catch (RuntimeException exception) {
            if (exception instanceof KeycloakIntegrationException integrationException) {
                throw integrationException;
            }
            throw new KeycloakIntegrationException(
                    "Unable to disable Keycloak user",
                    exception
            );
        }
    }

    private UserRepresentation resolveExisting(
            String username,
            String externalId
    ) {
        if (externalId != null) {
            try {
                return users().get(externalId).toRepresentation();
            } catch (NotFoundException ignored) {
                // The IdP may have been restored and generated new internal identifiers.
                // Fall back to the stable username and repair the binding on success.
            }
        }

        List<UserRepresentation> users = users().searchByUsername(username, true);
        return users.isEmpty() ? null : users.getFirst();
    }

    private UserRepresentation createUser(
            String username,
            boolean enabled
    ) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(username);
        user.setEnabled(enabled);

        try (Response response = users().create(user)) {
            int status = response.getStatus();

            if (status == HTTP_CREATED) {
                String userId = requireText(
                        CreatedResponseUtil.getCreatedId(response),
                        "Keycloak user id"
                );
                return users().get(userId).toRepresentation();
            }

            if (status == HTTP_CONFLICT) {
                UserRepresentation existing = resolveExisting(username, null);
                if (existing != null) {
                    return existing;
                }
                throw new KeycloakUserConflictException(username);
            }

            throw new KeycloakIntegrationException(
                    "Unable to create Keycloak user. HTTP " + status
            );
        }
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