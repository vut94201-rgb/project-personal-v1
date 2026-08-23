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
import org.keycloak.admin.client.resource.RoleScopeResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

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
            RoleScopeResource roleScope = realm()
                    .users()
                    .get(subject)
                    .roles()
                    .clientLevel(client.getId());

            boolean alreadyAssigned = roleScope.listAll().stream()
                    .anyMatch(existing -> sameRole(existing, role));

            if (!alreadyAssigned) {
                roleScope.add(List.of(role));
            }
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
            ClientRepresentation client = resolveClientOrNull(applicationCode);
            if (client == null) {
                return;
            }

            RoleRepresentation role = resolveRoleOrNull(client, normalizedRoleCode);
            if (role == null) {
                return;
            }

            RoleScopeResource roleScope = realm()
                    .users()
                    .get(subject)
                    .roles()
                    .clientLevel(client.getId());

            boolean assigned = roleScope.listAll().stream()
                    .anyMatch(existing -> sameRole(existing, role));

            if (assigned) {
                roleScope.remove(List.of(role));
            }
        } catch (ProcessingException exception) {
            throw new KeycloakIntegrationException(
                    "Unable to connect to Keycloak Admin API",
                    exception
            );
        } catch (WebApplicationException exception) {
            if (exception.getResponse().getStatus() == 404) {
                // Missing user/client/role means the requested mapping is already absent.
                return;
            }
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
        ClientRepresentation client = resolveClientOrNull(applicationCode);
        if (client == null) {
            throw new KeycloakIntegrationException(
                    "Keycloak client not found for application: " + applicationCode
            );
        }
        return client;
    }

    private ClientRepresentation resolveClientOrNull(String applicationCode) {
        String clientId = toClientId(applicationCode);
        List<ClientRepresentation> clients = realm()
                .clients()
                .findByClientId(clientId);

        return clients.isEmpty() ? null : clients.getFirst();
    }

    private RoleRepresentation resolveRole(
            ClientRepresentation client,
            String roleCode
    ) {
        RoleRepresentation role = resolveRoleOrNull(client, roleCode);
        if (role == null) {
            throw new KeycloakIntegrationException(
                    "Keycloak client role not found: " + roleCode
            );
        }
        return role;
    }

    private RoleRepresentation resolveRoleOrNull(
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
                return null;
            }
            throw exception;
        }
    }

    private RealmResource realm() {
        return keycloakAdminClient
                .realm(requireText(properties.realm(), "integration.keycloak.realm"));
    }

    private static boolean sameRole(
            RoleRepresentation left,
            RoleRepresentation right
    ) {
        if (left.getId() != null && right.getId() != null) {
            return Objects.equals(left.getId(), right.getId());
        }
        return Objects.equals(left.getName(), right.getName());
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