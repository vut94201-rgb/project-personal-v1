package com.hanyang.identity.identityservicev4mono.infrastructure.keycloak;

import com.hanyang.identity.identityservicev4mono.infrastructure.keycloak.access.KeycloakServicePrincipalAccessAdapter;
import com.hanyang.identity.identityservicev4mono.infrastructure.keycloak.config.KeycloakProperties;
import com.hanyang.identity.identityservicev4mono.infrastructure.keycloak.service_identity.KeycloakServicePrincipalAdapter;
import com.hanyang.identity.identityservicev4mono.service_identity.application.port.IdentityProviderServicePrincipalPort;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.*;
import org.keycloak.representations.idm.ClientRepresentation;
import org.keycloak.representations.idm.RoleRepresentation;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
class KeycloakServicePrincipalAccessAdapterTest {

    @Test
    void assignsApplicationClientRoleToServiceAccountUser() {
        Keycloak keycloak = mock(Keycloak.class);
        RealmResource realm = mock(RealmResource.class);
        ClientsResource clients = mock(ClientsResource.class);
        ClientResource serviceClient = mock(ClientResource.class);
        ClientResource applicationClientResource = mock(ClientResource.class);
        RolesResource roles = mock(RolesResource.class);
        RoleResource roleResource = mock(RoleResource.class);
        UsersResource users = mock(UsersResource.class);
        UserResource user = mock(UserResource.class);
        RoleMappingResource roleMapping = mock(RoleMappingResource.class);
        RoleScopeResource roleScope = mock(RoleScopeResource.class);
        RoleMappingResource clientScopeMapping = mock(RoleMappingResource.class);
        RoleScopeResource clientScope = mock(RoleScopeResource.class);

        when(keycloak.realm("hanyang")).thenReturn(realm);
        when(realm.clients()).thenReturn(clients);
        when(realm.users()).thenReturn(users);

        when(clients.get("kc-service-client-1")).thenReturn(serviceClient);
        UserRepresentation serviceAccount = new UserRepresentation();
        serviceAccount.setId("service-account-user-1");
        when(serviceClient.getServiceAccountUser()).thenReturn(serviceAccount);
        when(serviceClient.getScopeMappings()).thenReturn(clientScopeMapping);
        when(clientScopeMapping.clientLevel("kc-oqc-client-1"))
                .thenReturn(clientScope);

        ClientRepresentation applicationClient = new ClientRepresentation();
        applicationClient.setId("kc-oqc-client-1");
        applicationClient.setClientId("oqc");
        when(clients.findByClientId("oqc"))
                .thenReturn(List.of(applicationClient));
        when(clients.get("kc-oqc-client-1"))
                .thenReturn(applicationClientResource);
        when(applicationClientResource.roles()).thenReturn(roles);
        when(roles.get("OQC_LOT_IMPORTER")).thenReturn(roleResource);

        RoleRepresentation role = new RoleRepresentation();
        role.setId("kc-role-1");
        role.setName("OQC_LOT_IMPORTER");
        when(roleResource.toRepresentation()).thenReturn(role);

        when(users.get("service-account-user-1")).thenReturn(user);
        when(user.roles()).thenReturn(roleMapping);
        when(roleMapping.clientLevel("kc-oqc-client-1"))
                .thenReturn(roleScope);
        when(roleScope.listAll()).thenReturn(List.of());
        when(clientScope.listAll()).thenReturn(List.of());

        KeycloakServicePrincipalAccessAdapter adapter =
                new KeycloakServicePrincipalAccessAdapter(
                        keycloak,
                        new KeycloakProperties(
                                "http://localhost:18080",
                                "hanyang",
                                "identity-service-admin",
                                "secret"
                        )
                );

        adapter.assignRole(
                "kc-service-client-1",
                "OQC",
                "OQC_LOT_IMPORTER"
        );

        verify(roleScope).add(List.of(role));
        verify(clientScope).add(List.of(role));
    }

    @Test
    void existingRoleMappingIsIdempotent() {
        Keycloak keycloak = mock(Keycloak.class);
        RealmResource realm = mock(RealmResource.class);
        ClientsResource clients = mock(ClientsResource.class);
        ClientResource serviceClient = mock(ClientResource.class);
        ClientResource applicationClientResource = mock(ClientResource.class);
        RolesResource roles = mock(RolesResource.class);
        RoleResource roleResource = mock(RoleResource.class);
        UsersResource users = mock(UsersResource.class);
        UserResource user = mock(UserResource.class);
        RoleMappingResource roleMapping = mock(RoleMappingResource.class);
        RoleScopeResource roleScope = mock(RoleScopeResource.class);
        RoleMappingResource clientScopeMapping = mock(RoleMappingResource.class);
        RoleScopeResource clientScope = mock(RoleScopeResource.class);

        when(keycloak.realm("hanyang")).thenReturn(realm);
        when(realm.clients()).thenReturn(clients);
        when(realm.users()).thenReturn(users);

        when(clients.get("kc-service-client-1")).thenReturn(serviceClient);
        UserRepresentation serviceAccount = new UserRepresentation();
        serviceAccount.setId("service-account-user-1");
        when(serviceClient.getServiceAccountUser()).thenReturn(serviceAccount);
        when(serviceClient.getScopeMappings()).thenReturn(clientScopeMapping);
        when(clientScopeMapping.clientLevel("kc-oqc-client-1"))
                .thenReturn(clientScope);

        ClientRepresentation applicationClient = new ClientRepresentation();
        applicationClient.setId("kc-oqc-client-1");
        applicationClient.setClientId("oqc");
        when(clients.findByClientId("oqc"))
                .thenReturn(List.of(applicationClient));
        when(clients.get("kc-oqc-client-1"))
                .thenReturn(applicationClientResource);
        when(applicationClientResource.roles()).thenReturn(roles);
        when(roles.get("OQC_LOT_IMPORTER")).thenReturn(roleResource);

        RoleRepresentation role = new RoleRepresentation();
        role.setId("kc-role-1");
        role.setName("OQC_LOT_IMPORTER");
        when(roleResource.toRepresentation()).thenReturn(role);

        when(users.get("service-account-user-1")).thenReturn(user);
        when(user.roles()).thenReturn(roleMapping);
        when(roleMapping.clientLevel("kc-oqc-client-1"))
                .thenReturn(roleScope);
        when(roleScope.listAll()).thenReturn(List.of(role));
        when(clientScope.listAll()).thenReturn(List.of(role));

        KeycloakServicePrincipalAccessAdapter adapter =
                new KeycloakServicePrincipalAccessAdapter(
                        keycloak,
                        new KeycloakProperties(
                                "http://localhost:18080",
                                "hanyang",
                                "identity-service-admin",
                                "secret"
                        )
                );

        adapter.assignRole(
                "kc-service-client-1",
                "OQC",
                "OQC_LOT_IMPORTER"
        );

        verify(roleScope, never()).add(anyList());
        verify(clientScope, never()).add(anyList());
    }
}