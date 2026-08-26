package com.hanyang.identity.identityservicev4mono.infrastructure.keycloak;


import com.hanyang.identity.identityservicev4mono.account.application.port.IdentityProviderAccountPort;
import com.hanyang.identity.identityservicev4mono.infrastructure.keycloak.account.KeycloakAccountAdapter;
import com.hanyang.identity.identityservicev4mono.infrastructure.keycloak.config.KeycloakProperties;
import com.hanyang.identity.identityservicev4mono.infrastructure.keycloak.exception.KeycloakIntegrationException;
import com.hanyang.identity.identityservicev4mono.infrastructure.keycloak.federation.KeycloakLdapFederationManager;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;


class KeycloakAccountAdapterTest {

    @Test
    void pendingAccountResolvesLdapFederatedUserAndDisablesItWithoutCreatingLocalUser() {
        Keycloak keycloak = mock(Keycloak.class);
        RealmResource realm = mock(RealmResource.class);
        UsersResource users = mock(UsersResource.class);
        UserResource userResource = mock(UserResource.class);
        KeycloakLdapFederationManager federationManager =
                mock(KeycloakLdapFederationManager.class);

        when(federationManager.requireProviderId()).thenReturn("ldap-provider-001");
        when(keycloak.realm("hanyang")).thenReturn(realm);
        when(realm.users()).thenReturn(users);

        UserRepresentation federated = new UserRepresentation();
        federated.setId("kc-fed-001");
        federated.setUsername("emp001");
        federated.setFederationLink("ldap-provider-001");
        federated.setEnabled(true);

        when(users.searchByUsername("emp001", true))
                .thenReturn(List.of(federated));
        when(users.get("kc-fed-001")).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(federated);

        KeycloakAccountAdapter adapter = new KeycloakAccountAdapter(
                keycloak,
                new KeycloakProperties(
                        "http://localhost:18080",
                        "hanyang",
                        "identity-service-admin",
                        "secret"
                ),
                federationManager
        );

        IdentityProviderAccountPort.ProvisionedAccount result =
                adapter.ensureAccount("emp001", null, false);

        assertEquals("kc-fed-001", result.externalId());
        assertEquals("emp001", result.externalCode());
        assertFalse(federated.isEnabled());
        verify(userResource).update(federated);
        verify(users, never()).create(any(UserRepresentation.class));
    }

    @Test
    void localKeycloakUserIsRejectedInsteadOfBeingAcceptedAsFederatedIdentity() {
        Keycloak keycloak = mock(Keycloak.class);
        RealmResource realm = mock(RealmResource.class);
        UsersResource users = mock(UsersResource.class);
        KeycloakLdapFederationManager federationManager =
                mock(KeycloakLdapFederationManager.class);

        when(federationManager.requireProviderId()).thenReturn("ldap-provider-001");
        when(keycloak.realm("hanyang")).thenReturn(realm);
        when(realm.users()).thenReturn(users);

        UserRepresentation local = new UserRepresentation();
        local.setId("legacy-local-001");
        local.setUsername("emp001");
        local.setFederationLink(null);
        local.setEnabled(true);

        when(users.searchByUsername("emp001", true)).thenReturn(List.of(local));

        KeycloakAccountAdapter adapter = new KeycloakAccountAdapter(
                keycloak,
                new KeycloakProperties(
                        "http://localhost:18080",
                        "hanyang",
                        "identity-service-admin",
                        "secret"
                ),
                federationManager
        );

        KeycloakIntegrationException exception = assertThrows(
                KeycloakIntegrationException.class,
                () -> adapter.ensureAccount("emp001", null, false)
        );

        org.junit.jupiter.api.Assertions.assertTrue(
                exception.getMessage().contains("Legacy local Keycloak user conflicts")
        );
        verify(users, never()).create(any(UserRepresentation.class));
    }

    @Test
    void disablingFederatedUserAlsoRevokesExistingSessions() {
        Keycloak keycloak = mock(Keycloak.class);
        RealmResource realm = mock(RealmResource.class);
        UsersResource users = mock(UsersResource.class);
        UserResource userResource = mock(UserResource.class);
        KeycloakLdapFederationManager federationManager =
                mock(KeycloakLdapFederationManager.class);

        when(federationManager.requireProviderId()).thenReturn("ldap-provider-001");
        when(keycloak.realm("hanyang")).thenReturn(realm);
        when(realm.users()).thenReturn(users);

        UserRepresentation federated = new UserRepresentation();
        federated.setId("kc-fed-001");
        federated.setUsername("emp001");
        federated.setFederationLink("ldap-provider-001");
        federated.setEnabled(true);

        when(users.get("kc-fed-001")).thenReturn(userResource);
        when(userResource.toRepresentation()).thenReturn(federated);

        KeycloakAccountAdapter adapter = new KeycloakAccountAdapter(
                keycloak,
                new KeycloakProperties(
                        "http://localhost:18080",
                        "hanyang",
                        "identity-service-admin",
                        "secret"
                ),
                federationManager
        );

        IdentityProviderAccountPort.ProvisionedAccount result =
                adapter.disableAccount("emp001", "kc-fed-001");

        assertEquals("kc-fed-001", result.externalId());
        assertFalse(federated.isEnabled());
        verify(userResource).update(federated);
        verify(userResource).logout();
    }
}