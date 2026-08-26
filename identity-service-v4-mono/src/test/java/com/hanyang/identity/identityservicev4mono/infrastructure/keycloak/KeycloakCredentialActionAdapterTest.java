package com.hanyang.identity.identityservicev4mono.infrastructure.keycloak;

import com.hanyang.identity.identityservicev4mono.infrastructure.keycloak.account.KeycloakCredentialActionAdapter;
import com.hanyang.identity.identityservicev4mono.infrastructure.keycloak.config.KeycloakProperties;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.representations.idm.UserRepresentation;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

class KeycloakCredentialActionAdapterTest {

    @Test
    void requirePasswordChangeAddsUpdatePasswordWithoutStoringCredential() {
        Keycloak keycloak = mock(Keycloak.class);
        RealmResource realm = mock(RealmResource.class);
        UsersResource users = mock(UsersResource.class);
        UserResource userResource = mock(UserResource.class);

        when(keycloak.realm("hanyang")).thenReturn(realm);
        when(realm.users()).thenReturn(users);
        when(users.get("kc-fed-001")).thenReturn(userResource);

        UserRepresentation representation = new UserRepresentation();
        representation.setId("kc-fed-001");
        representation.setRequiredActions(new ArrayList<>());
        when(userResource.toRepresentation()).thenReturn(representation);

        KeycloakCredentialActionAdapter adapter =
                new KeycloakCredentialActionAdapter(
                        keycloak,
                        new KeycloakProperties(
                                "http://localhost:18080",
                                "hanyang",
                                "identity-service-admin",
                                "secret"
                        )
                );

        adapter.requirePasswordChange("kc-fed-001");

        assertEquals(List.of("UPDATE_PASSWORD"), representation.getRequiredActions());
        verify(userResource).update(representation);
        verify(userResource, never()).resetPassword(any());
    }

    @Test
    void requirePasswordChangeIsIdempotent() {
        Keycloak keycloak = mock(Keycloak.class);
        RealmResource realm = mock(RealmResource.class);
        UsersResource users = mock(UsersResource.class);
        UserResource userResource = mock(UserResource.class);

        when(keycloak.realm("hanyang")).thenReturn(realm);
        when(realm.users()).thenReturn(users);
        when(users.get("kc-fed-001")).thenReturn(userResource);

        UserRepresentation representation = new UserRepresentation();
        representation.setRequiredActions(new ArrayList<>(List.of("UPDATE_PASSWORD")));
        when(userResource.toRepresentation()).thenReturn(representation);

        KeycloakCredentialActionAdapter adapter =
                new KeycloakCredentialActionAdapter(
                        keycloak,
                        new KeycloakProperties(
                                "http://localhost:18080",
                                "hanyang",
                                "identity-service-admin",
                                "secret"
                        )
                );

        adapter.requirePasswordChange("kc-fed-001");

        verify(userResource, never()).update(any(UserRepresentation.class));
        verify(userResource, never()).resetPassword(any());
    }
}