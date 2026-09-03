package com.hanyang.identity.identityservicev4mono.infrastructure.keycloak.account;


import com.hanyang.identity.identityservicev4mono.account.application.port.IdentityProviderCredentialPolicyPort;
import com.hanyang.identity.identityservicev4mono.infrastructure.keycloak.config.KeycloakProperties;
import com.hanyang.identity.identityservicev4mono.infrastructure.keycloak.exception.KeycloakIntegrationException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class KeycloakCredentialPolicyAdapter
        implements IdentityProviderCredentialPolicyPort {

    private static final String UPDATE_PASSWORD = "UPDATE_PASSWORD";

    private final Keycloak keycloakAdminClient;
    private final KeycloakProperties properties;

    @Override
    public void clearPasswordChangeRequirement(String externalId) {
        String normalizedExternalId = requireText(
                externalId,
                "Keycloak user id"
        );

        try {
            UserResource userResource = user(normalizedExternalId);
            UserRepresentation representation = userResource.toRepresentation();

            List<String> existing = representation.getRequiredActions();

            if (existing == null || !existing.contains(UPDATE_PASSWORD)) {
                return;
            }

            List<String> requiredActions = new ArrayList<>(existing);
            requiredActions.removeIf(UPDATE_PASSWORD::equals);

            representation.setRequiredActions(requiredActions);
            userResource.update(representation);

        } catch (ProcessingException exception) {
            throw unableToConnect(exception);

        } catch (WebApplicationException exception) {
            throw new KeycloakIntegrationException(
                    "Unable to clear Keycloak password-change requirement. HTTP "
                            + exception.getResponse().getStatus(),
                    exception
            );

        } catch (RuntimeException exception) {
            if (exception instanceof KeycloakIntegrationException integrationException) {
                throw integrationException;
            }

            throw new KeycloakIntegrationException(
                    "Unable to clear Keycloak password-change requirement",
                    exception
            );
        }
    }

    private UserResource user(String externalId) {
        return keycloakAdminClient
                .realm(requireText(
                        properties.realm(),
                        "integration.keycloak.realm"
                ))
                .users()
                .get(externalId);
    }

    private static KeycloakIntegrationException unableToConnect(
            ProcessingException exception
    ) {
        return new KeycloakIntegrationException(
                "Unable to connect to Keycloak Admin API",
                exception
        );
    }

    private static String requireText(
            String value,
            String fieldName
    ) {
        if (value == null || value.isBlank()) {
            throw new KeycloakIntegrationException(
                    fieldName + " must not be blank"
            );
        }

        return value.trim();
    }
}