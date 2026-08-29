package com.hanyang.identity.identityservicev4mono.infrastructure.keycloak.access;


import com.hanyang.identity.identityservicev4mono.access.application.port.IdentityProviderServicePrincipalAccessPort;
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
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@Component
@RequiredArgsConstructor
public class KeycloakServicePrincipalAccessAdapter
        implements IdentityProviderServicePrincipalAccessPort {

    private static final int HTTP_NOT_FOUND = 404;

    private final Keycloak keycloakAdminClient;
    private final KeycloakProperties properties;

    @Override
    public void assignRole(
            String externalServicePrincipalId,
            String applicationCode,
            String roleCode
    ) {
        String serviceClientId = requireText(
                externalServicePrincipalId,
                "externalServicePrincipalId"
        );

        try {
            ClientResource serviceClient = realm()
                    .clients()
                    .get(serviceClientId);
            UserRepresentation serviceAccountUser =
                    requireServiceAccountUser(serviceClient, serviceClientId);
            ClientRepresentation applicationClient =
                    resolveApplicationClient(applicationCode);
            String applicationClientId = requireText(
                    applicationClient.getId(),
                    "Keycloak application client id"
            );
            RoleRepresentation role = resolveRole(
                    applicationClient,
                    requireText(roleCode, "roleCode")
            );

            RoleScopeResource serviceAccountRoleScope = realm()
                    .users()
                    .get(requireText(
                            serviceAccountUser.getId(),
                            "Keycloak service account user id"
                    ))
                    .roles()
                    .clientLevel(applicationClientId);

            ensureRoleAssigned(serviceAccountRoleScope, role);

            // Commit 5 deliberately sets fullScopeAllowed=false. Keycloak
            // service-account token roles are the intersection of service
            // account roles and the client's role-scope mappings, so the same
            // role must also be explicitly allowed in the machine client's
            // dedicated scope.
            RoleScopeResource clientScope = serviceClient
                    .getScopeMappings()
                    .clientLevel(applicationClientId);

            ensureRoleAssigned(clientScope, role);
        } catch (ProcessingException exception) {
            throw new KeycloakIntegrationException(
                    "Unable to connect to Keycloak Admin API",
                    exception
            );
        } catch (WebApplicationException exception) {
            throw new KeycloakIntegrationException(
                    "Unable to assign Keycloak role to service principal. HTTP "
                            + exception.getResponse().getStatus(),
                    exception
            );
        } catch (RuntimeException exception) {
            if (exception instanceof KeycloakIntegrationException integrationException) {
                throw integrationException;
            }
            throw new KeycloakIntegrationException(
                    "Unable to assign Keycloak role to service principal",
                    exception
            );
        }
    }

    @Override
    public void revokeRole(
            String externalServicePrincipalId,
            String applicationCode,
            String roleCode
    ) {
        String serviceClientId = requireText(
                externalServicePrincipalId,
                "externalServicePrincipalId"
        );

        try {
            ClientResource serviceClient = realm()
                    .clients()
                    .get(serviceClientId);
            ClientRepresentation applicationClient =
                    resolveApplicationClientOrNull(applicationCode);
            if (applicationClient == null) {
                return;
            }

            String applicationClientId = requireText(
                    applicationClient.getId(),
                    "Keycloak application client id"
            );
            RoleRepresentation role = resolveRoleOrNull(
                    applicationClient,
                    requireText(roleCode, "roleCode")
            );
            if (role == null) {
                return;
            }

            UserRepresentation serviceAccountUser =
                    serviceAccountUserOrNull(serviceClient);
            if (serviceAccountUser != null
                    && serviceAccountUser.getId() != null
                    && !serviceAccountUser.getId().isBlank()) {
                RoleScopeResource serviceAccountRoleScope = realm()
                        .users()
                        .get(serviceAccountUser.getId().trim())
                        .roles()
                        .clientLevel(applicationClientId);

                removeRoleIfAssigned(serviceAccountRoleScope, role);
            }

            RoleScopeResource clientScope = serviceClient
                    .getScopeMappings()
                    .clientLevel(applicationClientId);
            removeRoleIfAssigned(clientScope, role);
        } catch (ProcessingException exception) {
            throw new KeycloakIntegrationException(
                    "Unable to connect to Keycloak Admin API",
                    exception
            );
        } catch (WebApplicationException exception) {
            if (exception.getResponse().getStatus() == HTTP_NOT_FOUND) {
                return;
            }
            throw new KeycloakIntegrationException(
                    "Unable to revoke Keycloak role from service principal. HTTP "
                            + exception.getResponse().getStatus(),
                    exception
            );
        } catch (RuntimeException exception) {
            if (exception instanceof KeycloakIntegrationException integrationException) {
                throw integrationException;
            }
            throw new KeycloakIntegrationException(
                    "Unable to revoke Keycloak role from service principal",
                    exception
            );
        }
    }

    private UserRepresentation requireServiceAccountUser(
            ClientResource serviceClient,
            String serviceClientId
    ) {
        UserRepresentation user = serviceAccountUserOrNull(serviceClient);
        if (user == null || user.getId() == null || user.getId().isBlank()) {
            throw new KeycloakIntegrationException(
                    "Keycloak service account user not found for service-principal client: "
                            + serviceClientId
            );
        }
        return user;
    }

    private UserRepresentation serviceAccountUserOrNull(
            ClientResource serviceClient
    ) {
        try {
            return serviceClient.getServiceAccountUser();
        } catch (WebApplicationException exception) {
            if (exception.getResponse().getStatus() == HTTP_NOT_FOUND) {
                return null;
            }
            throw exception;
        }
    }

    private static void ensureRoleAssigned(
            RoleScopeResource roleScope,
            RoleRepresentation role
    ) {
        boolean assigned = roleScope.listAll().stream()
                .anyMatch(existing -> sameRole(existing, role));

        if (!assigned) {
            roleScope.add(List.of(role));
        }
    }

    private static void removeRoleIfAssigned(
            RoleScopeResource roleScope,
            RoleRepresentation role
    ) {
        boolean assigned = roleScope.listAll().stream()
                .anyMatch(existing -> sameRole(existing, role));

        if (assigned) {
            roleScope.remove(List.of(role));
        }
    }

    private ClientRepresentation resolveApplicationClient(
            String applicationCode
    ) {
        ClientRepresentation client =
                resolveApplicationClientOrNull(applicationCode);
        if (client == null) {
            throw new KeycloakIntegrationException(
                    "Keycloak client not found for application: "
                            + applicationCode
            );
        }
        return client;
    }

    private ClientRepresentation resolveApplicationClientOrNull(
            String applicationCode
    ) {
        String clientId = requireText(
                applicationCode,
                "applicationCode"
        ).toLowerCase(Locale.ROOT);

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
                .get(requireText(
                        client.getId(),
                        "Keycloak application client id"
                ));

        try {
            return clientResource
                    .roles()
                    .get(roleCode)
                    .toRepresentation();
        } catch (WebApplicationException exception) {
            if (exception.getResponse().getStatus() == HTTP_NOT_FOUND) {
                return null;
            }
            throw exception;
        }
    }

    private RealmResource realm() {
        return keycloakAdminClient.realm(
                requireText(
                        properties.realm(),
                        "integration.keycloak.realm"
                )
        );
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