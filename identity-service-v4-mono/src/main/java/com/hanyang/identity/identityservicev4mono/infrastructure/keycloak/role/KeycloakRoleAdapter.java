package com.hanyang.identity.identityservicev4mono.infrastructure.keycloak.role;


import com.hanyang.identity.identityservicev4mono.access.application.port.IdentityProviderRolePort;
import com.hanyang.identity.identityservicev4mono.infrastructure.keycloak.config.KeycloakProperties;
import com.hanyang.identity.identityservicev4mono.infrastructure.keycloak.exception.KeycloakIntegrationException;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ClientResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RoleResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class KeycloakRoleAdapter implements IdentityProviderRolePort {

    private static final int HTTP_NOT_FOUND = 404;
    private static final int HTTP_CONFLICT = 409;

    private final Keycloak keycloakAdminClient;
    private final KeycloakProperties properties;

    @Override
    public ProvisionedRole synchronizeRole(
            String applicationCode,
            String roleCode,
            String roleName,
            boolean active
    ) {
        String clientId = toClientId(applicationCode);
        String normalizedRoleCode = requireText(roleCode, "roleCode");
        String normalizedRoleName = requireText(roleName, "roleName");

        try {
            ClientRepresentation client = requireClient(clientId, applicationCode);
            ClientResource clientResource = realm()
                    .clients()
                    .get(requireText(client.getId(), "Keycloak client id"));
            RolesResource roles = clientResource.roles();

            if (!active) {
                removeRoleIfPresent(roles, normalizedRoleCode);
                return new ProvisionedRole(null, normalizedRoleCode);
            }

            RoleRepresentation role = resolveRole(roles, normalizedRoleCode);
            if (role == null) {
                role = createRole(roles, normalizedRoleCode, normalizedRoleName);
            }

            RoleRepresentation synchronizedRole = synchronizeManagedFields(
                    roles,
                    role,
                    normalizedRoleName
            );

            return new IdentityProviderRolePort.ProvisionedRole(
                    requireText(synchronizedRole.getId(), "Keycloak role id"),
                    requireText(synchronizedRole.getName(), "Keycloak role name")
            );
        } catch (ProcessingException exception) {
            throw new KeycloakIntegrationException(
                    "Unable to connect to Keycloak Admin API",
                    exception
            );
        } catch (WebApplicationException exception) {
            throw new KeycloakIntegrationException(
                    "Unable to synchronize Keycloak client role. HTTP "
                            + exception.getResponse().getStatus(),
                    exception
            );
        } catch (RuntimeException exception) {
            if (exception instanceof KeycloakIntegrationException integrationException) {
                throw integrationException;
            }
            throw new KeycloakIntegrationException(
                    "Unable to synchronize Keycloak client role",
                    exception
            );
        }
    }

    private RoleRepresentation createRole(
            RolesResource roles,
            String roleCode,
            String roleName
    ) {
        RoleRepresentation representation = new RoleRepresentation();
        representation.setName(roleCode);
        representation.setDescription(roleName);

        try {
            roles.create(representation);
        } catch (WebApplicationException exception) {
            if (exception.getResponse().getStatus() != HTTP_CONFLICT) {
                throw exception;
            }
        }

        RoleRepresentation created = resolveRole(roles, roleCode);
        if (created == null) {
            throw new KeycloakIntegrationException(
                    "Keycloak client role was not found after creation: " + roleCode
            );
        }
        return created;
    }

    private RoleRepresentation synchronizeManagedFields(
            RolesResource roles,
            RoleRepresentation role,
            String roleName
    ) {
        String roleCode = requireText(role.getName(), "Keycloak role name");
        RoleResource resource = roles.get(roleCode);
        RoleRepresentation current = resource.toRepresentation();

        if (!Objects.equals(current.getDescription(), roleName)) {
            current.setDescription(roleName);
            resource.update(current);
            current = resource.toRepresentation();
        }

        return current;
    }

    private void removeRoleIfPresent(
            RolesResource roles,
            String roleCode
    ) {
        RoleRepresentation existing = resolveRole(roles, roleCode);
        if (existing == null) {
            return;
        }

        roles.get(roleCode).remove();
    }

    private RoleRepresentation resolveRole(
            RolesResource roles,
            String roleCode
    ) {
        try {
            return roles.get(roleCode).toRepresentation();
        } catch (WebApplicationException exception) {
            if (exception.getResponse().getStatus() == HTTP_NOT_FOUND) {
                return null;
            }
            throw exception;
        }
    }

    private ClientRepresentation requireClient(
            String clientId,
            String applicationCode
    ) {
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