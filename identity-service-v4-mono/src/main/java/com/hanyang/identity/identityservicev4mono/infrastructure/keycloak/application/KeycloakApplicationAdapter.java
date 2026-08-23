package com.hanyang.identity.identityservicev4mono.infrastructure.keycloak.application;


import com.hanyang.identity.identityservicev4mono.access.application.port.IdentityProviderApplicationPort;
import com.hanyang.identity.identityservicev4mono.infrastructure.keycloak.config.KeycloakProperties;
import com.hanyang.identity.identityservicev4mono.infrastructure.keycloak.exception.KeycloakIntegrationException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class KeycloakApplicationAdapter implements IdentityProviderApplicationPort {

    private static final int HTTP_CREATED = 201;
    private static final int HTTP_CONFLICT = 409;

    private final Keycloak keycloakAdminClient;
    private final KeycloakProperties properties;

    @Override
    public ProvisionedApplication ensureApplication(
            String applicationCode,
            String applicationName,
            boolean enabled
    ) {
        String clientId = toClientId(applicationCode);
        String name = requireText(applicationName, "applicationName");

        try {
            ClientRepresentation client = resolveClient(clientId);

            if (client == null) {
                client = createClient(clientId, name, enabled);
            }

            ClientRepresentation synchronizedClient = synchronizeManagedFields(
                    client,
                    name,
                    enabled
            );

            return new IdentityProviderApplicationPort.ProvisionedApplication(
                    requireText(synchronizedClient.getId(), "Keycloak client id"),
                    requireText(synchronizedClient.getClientId(), "Keycloak clientId")
            );
        } catch (ProcessingException exception) {
            throw new KeycloakIntegrationException(
                    "Unable to connect to Keycloak Admin API",
                    exception
            );
        } catch (WebApplicationException exception) {
            throw new KeycloakIntegrationException(
                    "Unable to synchronize Keycloak client. HTTP "
                            + exception.getResponse().getStatus(),
                    exception
            );
        } catch (RuntimeException exception) {
            if (exception instanceof KeycloakIntegrationException integrationException) {
                throw integrationException;
            }
            throw new KeycloakIntegrationException(
                    "Unable to synchronize Keycloak client",
                    exception
            );
        }
    }

    private ClientRepresentation createClient(
            String clientId,
            String name,
            boolean enabled
    ) {
        ClientRepresentation representation = new ClientRepresentation();
        representation.setClientId(clientId);
        representation.setName(name);
        representation.setEnabled(enabled);
        representation.setProtocol("openid-connect");

        try (Response response = realm().clients().create(representation)) {
            int status = response.getStatus();

            if (status == HTTP_CREATED) {
                String createdId = requireText(
                        CreatedResponseUtil.getCreatedId(response),
                        "Keycloak client id"
                );
                return realm().clients().get(createdId).toRepresentation();
            }

            if (status == HTTP_CONFLICT) {
                ClientRepresentation existing = resolveClient(clientId);
                if (existing != null) {
                    return existing;
                }
            }

            throw new KeycloakIntegrationException(
                    "Unable to create Keycloak client "
                            + clientId
                            + ". HTTP "
                            + status
            );
        }
    }

    private ClientRepresentation synchronizeManagedFields(
            ClientRepresentation client,
            String name,
            boolean enabled
    ) {
        String internalId = requireText(client.getId(), "Keycloak client id");
        ClientResource resource = realm().clients().get(internalId);
        ClientRepresentation current = resource.toRepresentation();

        boolean changed = false;

        if (!Objects.equals(current.getName(), name)) {
            current.setName(name);
            changed = true;
        }

        if (!Objects.equals(current.isEnabled(), enabled)) {
            current.setEnabled(enabled);
            changed = true;
        }

        if (changed) {
            resource.update(current);
            current = resource.toRepresentation();
        }

        return current;
    }

    private ClientRepresentation resolveClient(String clientId) {
        List<ClientRepresentation> clients = realm()
                .clients()
                .findByClientId(clientId);

        if (clients.isEmpty()) {
            return null;
        }

        return clients.getFirst();
    }

    private RealmResource realm() {
        return keycloakAdminClient.realm(
                requireText(properties.realm(), "integration.keycloak.realm")
        );
    }

    private static String toClientId(String applicationCode) {
        return requireText(applicationCode, "applicationCode")
                .toLowerCase(Locale.ROOT);
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new KeycloakIntegrationException(fieldName + " must not be blank");
        }
        return value.trim();
    }
}