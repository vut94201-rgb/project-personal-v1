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
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KeycloakAccountAdapter
        implements IdentityProviderAccountPort {

    private static final int HTTP_CREATED = 201;
    private static final int HTTP_CONFLICT = 409;

    private final Keycloak keycloakAdminClient;
    private final KeycloakProperties properties;

    @Override
    public String createUser(String username) {
        UserRepresentation user = new UserRepresentation();
        user.setUsername(requireText(username, "username"));
        user.setEnabled(true);

        try (Response response = users().create(user)) {
            int status = response.getStatus();

            if (status == HTTP_CREATED) {
                String userId = CreatedResponseUtil.getCreatedId(response);
                return requireText(userId, "Keycloak user id");
            }

            if (status == HTTP_CONFLICT) {
                throw new KeycloakUserConflictException(username);
            }

            throw new KeycloakIntegrationException(
                    "Unable to create Keycloak user. HTTP " + status
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
                    "Unable to create Keycloak user. HTTP "
                            + exception.getResponse().getStatus(),
                    exception
            );
        } catch (RuntimeException exception) {
            if (exception instanceof KeycloakIntegrationException integrationException) {
                throw integrationException;
            }
            throw new KeycloakIntegrationException(
                    "Unable to create Keycloak user",
                    exception
            );
        }
    }

    @Override
    public void disableUser(String subject) {
        try {
            UserResource userResource = users().get(requireText(subject, "subject"));
            UserRepresentation user = userResource.toRepresentation();
            user.setEnabled(false);
            userResource.update(user);
        } catch (NotFoundException ignored) {
            // Already absent in Keycloak means the external account is effectively disabled.
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

    private org.keycloak.admin.client.resource.UsersResource users() {
        return keycloakAdminClient
                .realm(requireText(properties.realm(), "integration.keycloak.realm"))
                .users();
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