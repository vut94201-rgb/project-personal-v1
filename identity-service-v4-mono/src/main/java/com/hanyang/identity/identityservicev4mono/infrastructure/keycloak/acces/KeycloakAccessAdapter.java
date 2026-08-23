package com.hanyang.identity.identityservicev4mono.infrastructure.keycloak.acces;

import com.hanyang.identity.identityservicev4mono.access.application.port.IdentityProviderAccessPort;
import com.hanyang.identity.identityservicev4mono.infrastructure.keycloak.config.KeycloakProperties;
import com.hanyang.identity.identityservicev4mono.infrastructure.keycloak.exception.KeycloakIntegrationException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;

@Component
@RequiredArgsConstructor
public class KeycloakAccessAdapter implements IdentityProviderAccessPort {

    private final Keycloak keycloakAdminClient;
    private final KeycloakProperties properties;

    @Override
    public void assignRole(
            String keycloakSubject,
            String applicationCode,
            String roleCode
    ) {
        String subject = requireText(keycloakSubject, "keycloakSubject");
        String normalizedRoleCode = requireText(roleCode, "roleCode");

        try {
            ClientRepresentation client = resolveClient(applicationCode);
            RoleRepresentation role = resolveRole(client, normalizedRoleCode);

            realm()
                    .users()
                    .get(subject)
                    .roles()
                    .clientLevel(client.getId())
                    .add(List.of(role));
        } catch (ProcessingException exception) {
            throw new KeycloakIntegrationException(
                    "Unable to connect to Keycloak Admin API",
                    exception
            );
        } catch (WebApplicationException exception) {
            throw new KeycloakIntegrationException(
                    "Unable to assign Keycloak role. HTTP "
                            + exception.getResponse().getStatus(),
                    exception
            );
        } catch (RuntimeException exception) {
            if (exception instanceof KeycloakIntegrationException integrationException) {
                throw integrationException;
            }
            throw new KeycloakIntegrationException(
                    "Unable to assign Keycloak role",
                    exception
            );
        }
    }

    @Override
    public void revokeRole(
            String keycloakSubject,
            String applicationCode,
            String roleCode
    ) {
        String subject = requireText(keycloakSubject, "keycloakSubject");
        String normalizedRoleCode = requireText(roleCode, "roleCode");

        try {
            ClientRepresentation client = resolveClient(applicationCode);
            RoleRepresentation role = resolveRole(client, normalizedRoleCode);

            realm()
                    .users()
                    .get(subject)
                    .roles()
                    .clientLevel(client.getId())
                    .remove(List.of(role));
        } catch (ProcessingException exception) {
            throw new KeycloakIntegrationException(
                    "Unable to connect to Keycloak Admin API",
                    exception
            );
        } catch (WebApplicationException exception) {
            throw new KeycloakIntegrationException(
                    "Unable to revoke Keycloak role. HTTP "
                            + exception.getResponse().getStatus(),
                    exception
            );
        } catch (RuntimeException exception) {
            if (exception instanceof KeycloakIntegrationException integrationException) {
                throw integrationException;
            }
            throw new KeycloakIntegrationException(
                    "Unable to revoke Keycloak role",
                    exception
            );
        }
    }

    private ClientRepresentation resolveClient(String applicationCode) {
        String clientId = toClientId(applicationCode);
        List<ClientRepresentation> clients = realm()
                .clients()
                .findByClientId(clientId);

        if (clients.isEmpty()) {
            throw new KeycloakIntegrationException(
                    "Keycloak client not found for application: " + applicationCode
            );
        }

        return clients.getFirst();
    }

    private RoleRepresentation resolveRole(
            ClientRepresentation client,
            String roleCode
    ) {
        ClientResource clientResource = realm()
                .clients()
                .get(requireText(client.getId(), "Keycloak client id"));

        try {
            return clientResource
                    .roles()
                    .get(roleCode)
                    .toRepresentation();
        } catch (WebApplicationException exception) {
            if (exception.getResponse().getStatus() == 404) {
                throw new KeycloakIntegrationException(
                        "Keycloak client role not found: " + roleCode
                );
            }
            throw exception;
        }
    }

    private RealmResource realm() {
        return keycloakAdminClient
                .realm(requireText(properties.realm(), "integration.keycloak.realm"));
    }

    private static String toClientId(String applicationCode) {
        return requireText(applicationCode, "applicationCode")
                .toLowerCase(Locale.ROOT);
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