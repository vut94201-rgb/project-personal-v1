package com.hanyang.identity.identityservicev4mono.infrastructure.keycloak.federation;
import com.hanyang.identity.identityservicev4mono.infrastructure.keycloak.config.KeycloakLdapFederationProperties;
import com.hanyang.identity.identityservicev4mono.infrastructure.keycloak.config.KeycloakProperties;
import com.hanyang.identity.identityservicev4mono.infrastructure.ldap.ds389.config.Ds389Properties;
import jakarta.ws.rs.core.Response;
import org.junit.jupiter.api.Test;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ComponentsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.representations.idm.ComponentRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.mockito.ArgumentCaptor;

import java.net.URI;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class KeycloakLdapFederationManagerTest {

    @Test
    void creates389DsFederationWithExpectedDirectorySemantics() {
        Keycloak keycloak = mock(Keycloak.class);
        RealmResource realm = mock(RealmResource.class);
        ComponentsResource components = mock(ComponentsResource.class);

        when(keycloak.realm("hanyang")).thenReturn(realm);
        when(realm.components()).thenReturn(components);

        RealmRepresentation realmRepresentation = new RealmRepresentation();
        realmRepresentation.setId("realm-001");
        when(realm.toRepresentation()).thenReturn(realmRepresentation);
        when(components.query(
                "realm-001",
                KeycloakLdapFederationManager.USER_STORAGE_PROVIDER_TYPE,
                "hanyang-389ds"
        )).thenReturn(List.of());

        Response createdResponse = Response.created(
                URI.create("http://localhost/admin/realms/hanyang/components/ldap-provider-001")
        ).build();
        when(components.add(any(ComponentRepresentation.class)))
                .thenReturn(createdResponse);

        KeycloakLdapFederationManager manager = new KeycloakLdapFederationManager(
                keycloak,
                new KeycloakProperties(
                        "http://localhost:18080",
                        "hanyang",
                        "identity-service-admin",
                        "secret"
                ),
                new KeycloakLdapFederationProperties(
                        true,
                        "hanyang-389ds",
                        "ldap://host.containers.internal:3389",
                        "WRITABLE",
                        false
                ),
                new Ds389Properties(
                        true,
                        "ldap://localhost:3389",
                        "dc=hanyang,dc=local",
                        "cn=Directory Manager",
                        "change_me",
                        "ou=People",
                        false
                )
        );

        String providerId = manager.ensureConfigured();

        assertEquals("ldap-provider-001", providerId);

        ArgumentCaptor<ComponentRepresentation> captor =
                ArgumentCaptor.forClass(ComponentRepresentation.class);
        verify(components).add(captor.capture());

        ComponentRepresentation component = captor.getValue();
        assertEquals("hanyang-389ds", component.getName());
        assertEquals("ldap", component.getProviderId());
        assertEquals(
                KeycloakLdapFederationManager.USER_STORAGE_PROVIDER_TYPE,
                component.getProviderType()
        );
        assertEquals("realm-001", component.getParentId());
        assertEquals(
                "ldap://host.containers.internal:3389",
                component.getConfig().getFirst("connectionUrl")
        );
        assertEquals(
                "ou=People,dc=hanyang,dc=local",
                component.getConfig().getFirst("usersDn")
        );
        assertEquals("uid", component.getConfig().getFirst("usernameLDAPAttribute"));
        assertEquals("uid", component.getConfig().getFirst("rdnLDAPAttribute"));
        assertEquals("nsUniqueId", component.getConfig().getFirst("uuidLDAPAttribute"));
        assertEquals("true", component.getConfig().getFirst("importEnabled"));
        assertEquals("false", component.getConfig().getFirst("syncRegistrations"));
        assertEquals("WRITABLE", component.getConfig().getFirst("editMode"));
        assertEquals("cn=Directory Manager", component.getConfig().getFirst("bindDn"));
        assertEquals("change_me", component.getConfig().getFirst("bindCredential"));
    }
}