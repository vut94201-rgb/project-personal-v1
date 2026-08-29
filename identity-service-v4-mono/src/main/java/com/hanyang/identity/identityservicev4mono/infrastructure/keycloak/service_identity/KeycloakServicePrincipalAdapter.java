package com.hanyang.identity.identityservicev4mono.infrastructure.keycloak.service_identity;


import com.hanyang.identity.identityservicev4mono.infrastructure.keycloak.config.KeycloakProperties;
import com.hanyang.identity.identityservicev4mono.infrastructure.keycloak.exception.KeycloakIntegrationException;
import com.hanyang.identity.identityservicev4mono.service_identity.application.port.IdentityProviderServicePrincipalPort;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class KeycloakServicePrincipalAdapter
        implements IdentityProviderServicePrincipalPort {

    private static final int HTTP_CREATED = 201;
    private static final int HTTP_NOT_FOUND = 404;
    private static final int HTTP_CONFLICT = 409;
    private static final String CLIENT_AUTHENTICATOR = "client-secret";

    private final Keycloak keycloakAdminClient;
    private final KeycloakProperties properties;

    @Override
    public IdentityProviderServicePrincipalPort.ProvisionedServicePrincipal synchronizeServicePrincipal(
            String code,
            String displayName,
            String purpose,
            String externalId,
            boolean enabled
    ) {
        String clientId = toClientId(code);
        String name = requireText(displayName, "displayName");
        String description = requireText(purpose, "purpose");

        try {
            ClientRepresentation client = resolveClient(
                    externalId,
                    clientId
            );

            if (client == null) {
                client = createClient(
                        clientId,
                        name,
                        description,
                        enabled
                );
            }

            ClientRepresentation synchronizedClient = synchronizeManagedFields(
                    client,
                    name,
                    description,
                    enabled
            );

            // serviceAccountsEnabled should result in a dedicated service
            // account user. Resolve it here so a falsely "successful" client
            // configuration cannot be persisted as a healthy binding.
            UserRepresentation serviceAccountUser = realm()
                    .clients()
                    .get(requireText(
                            synchronizedClient.getId(),
                            "Keycloak client id"
                    ))
                    .getServiceAccountUser();

            requireText(
                    serviceAccountUser == null
                            ? null
                            : serviceAccountUser.getId(),
                    "Keycloak service account user id"
            );

            return new ProvisionedServicePrincipal(
                    requireText(
                            synchronizedClient.getId(),
                            "Keycloak client id"
                    ),
                    requireText(
                            synchronizedClient.getClientId(),
                            "Keycloak clientId"
                    )
            );
        } catch (ProcessingException exception) {
            throw new KeycloakIntegrationException(
                    "Unable to connect to Keycloak Admin API",
                    exception
            );
        } catch (WebApplicationException exception) {
            throw new KeycloakIntegrationException(
                    "Unable to synchronize Keycloak service principal. HTTP "
                            + exception.getResponse().getStatus(),
                    exception
            );
        } catch (RuntimeException exception) {
            if (exception instanceof KeycloakIntegrationException integrationException) {
                throw integrationException;
            }
            throw new KeycloakIntegrationException(
                    "Unable to synchronize Keycloak service principal",
                    exception
            );
        }
    }

    private ClientRepresentation createClient(
            String clientId,
            String name,
            String description,
            boolean enabled
    ) {
        ClientRepresentation representation = new ClientRepresentation();
        applyManagedFields(
                representation,
                clientId,
                name,
                description,
                enabled
        );

        try (Response response = realm().clients().create(representation)) {
            int status = response.getStatus();

            if (status == HTTP_CREATED) {
                String createdId = requireText(
                        CreatedResponseUtil.getCreatedId(response),
                        "Keycloak client id"
                );
                return realm()
                        .clients()
                        .get(createdId)
                        .toRepresentation();
            }

            if (status == HTTP_CONFLICT) {
                ClientRepresentation existing =
                        resolveClient(null, clientId);
                if (existing != null) {
                    return existing;
                }
            }

            throw new KeycloakIntegrationException(
                    "Unable to create Keycloak service-principal client "
                            + clientId
                            + ". HTTP "
                            + status
            );
        }
    }

    private ClientRepresentation synchronizeManagedFields(
            ClientRepresentation client,
            String name,
            String description,
            boolean enabled
    ) {
        String internalId = requireText(
                client.getId(),
                "Keycloak client id"
        );
        ClientResource resource = realm().clients().get(internalId);
        ClientRepresentation current = resource.toRepresentation();

        boolean changed = false;

        if (!Objects.equals(current.getName(), name)) {
            current.setName(name);
            changed = true;
        }
        if (!Objects.equals(current.getDescription(), description)) {
            current.setDescription(description);
            changed = true;
        }
        if (!Objects.equals(current.isEnabled(), enabled)) {
            current.setEnabled(enabled);
            changed = true;
        }
        if (!Objects.equals(current.getProtocol(), "openid-connect")) {
            current.setProtocol("openid-connect");
            changed = true;
        }
        if (!Objects.equals(current.isPublicClient(), Boolean.FALSE)) {
            current.setPublicClient(false);
            changed = true;
        }
        if (!Objects.equals(current.isBearerOnly(), Boolean.FALSE)) {
            current.setBearerOnly(false);
            changed = true;
        }
        if (!Objects.equals(current.isStandardFlowEnabled(), Boolean.FALSE)) {
            current.setStandardFlowEnabled(false);
            changed = true;
        }
        if (!Objects.equals(current.isImplicitFlowEnabled(), Boolean.FALSE)) {
            current.setImplicitFlowEnabled(false);
            changed = true;
        }
        if (!Objects.equals(
                current.isDirectAccessGrantsEnabled(),
                Boolean.FALSE
        )) {
            current.setDirectAccessGrantsEnabled(false);
            changed = true;
        }
        if (!Objects.equals(current.isServiceAccountsEnabled(), Boolean.TRUE)) {
            current.setServiceAccountsEnabled(true);
            changed = true;
        }
        if (!Objects.equals(current.isFullScopeAllowed(), Boolean.FALSE)) {
            current.setFullScopeAllowed(false);
            changed = true;
        }
        if (!Objects.equals(
                current.getClientAuthenticatorType(),
                CLIENT_AUTHENTICATOR
        )) {
            current.setClientAuthenticatorType(CLIENT_AUTHENTICATOR);
            changed = true;
        }

        if (changed) {
            resource.update(current);
            current = resource.toRepresentation();
        }

        return current;
    }

    private ClientRepresentation resolveClient(
            String externalId,
            String clientId
    ) {
        if (externalId != null && !externalId.isBlank()) {
            try {
                return realm()
                        .clients()
                        .get(externalId.trim())
                        .toRepresentation();
            } catch (WebApplicationException exception) {
                if (exception.getResponse().getStatus() != HTTP_NOT_FOUND) {
                    throw exception;
                }
                // Binding may point at a deleted/recreated client. Fall back
                // to stable business clientId and repair external_id.
            }
        }

        List<ClientRepresentation> clients = realm()
                .clients()
                .findByClientId(clientId);

        return clients.isEmpty() ? null : clients.getFirst();
    }

    private static void applyManagedFields(
            ClientRepresentation representation,
            String clientId,
            String name,
            String description,
            boolean enabled
    ) {
        representation.setClientId(clientId);
        representation.setName(name);
        representation.setDescription(description);
        representation.setEnabled(enabled);
        representation.setProtocol("openid-connect");
        representation.setPublicClient(false);
        representation.setBearerOnly(false);
        representation.setStandardFlowEnabled(false);
        representation.setImplicitFlowEnabled(false);
        representation.setDirectAccessGrantsEnabled(false);
        representation.setServiceAccountsEnabled(true);
        representation.setFullScopeAllowed(false);
        representation.setClientAuthenticatorType(CLIENT_AUTHENTICATOR);
    }

    private RealmResource realm() {
        return keycloakAdminClient.realm(
                requireText(
                        properties.realm(),
                        "integration.keycloak.realm"
                )
        );
    }

    private static String toClientId(String code) {
        return "svc-"
                + requireText(code, "code")
                .toLowerCase(Locale.ROOT)
                .replace('_', '-');
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