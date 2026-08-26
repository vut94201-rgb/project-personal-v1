package com.hanyang.identity.identityservicev4mono.infrastructure.keycloak.federation;


import com.hanyang.identity.identityservicev4mono.infrastructure.keycloak.config.KeycloakLdapFederationProperties;
import com.hanyang.identity.identityservicev4mono.infrastructure.keycloak.config.KeycloakProperties;
import com.hanyang.identity.identityservicev4mono.infrastructure.keycloak.exception.KeycloakIntegrationException;
import com.hanyang.identity.identityservicev4mono.infrastructure.ldap.ds389.config.Ds389Properties;
import jakarta.ws.rs.ProcessingException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import lombok.RequiredArgsConstructor;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.ComponentsResource;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.common.util.MultivaluedHashMap;
import org.keycloak.representations.idm.ComponentRepresentation;
import org.keycloak.representations.idm.RealmRepresentation;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

/**
 * Idempotently manages the Keycloak LDAP user-storage provider that points at
 * the Hanyang 389 Directory Server.
 *
 * <p>The provider itself is Keycloak infrastructure. Business account state
 * remains owned by Identity Service; LDAP identities remain owned by 389 DS.</p>
 */
@Component
@RequiredArgsConstructor
public class KeycloakLdapFederationManager {

    static final String USER_STORAGE_PROVIDER_TYPE =
            "org.keycloak.storage.UserStorageProvider";
    static final String LDAP_PROVIDER_ID = "ldap";

    private static final int HTTP_CREATED = 201;

    private final Keycloak keycloakAdminClient;
    private final KeycloakProperties keycloakProperties;
    private final KeycloakLdapFederationProperties federationProperties;
    private final Ds389Properties ds389Properties;

    /**
     * Ensures the provider exists and converges the configuration controlled by
     * Identity Service. Existing provider-specific metadata such as lastSync is
     * preserved.
     */
    public String ensureConfigured() {
        requireFederationEnabled();
        try {
            RealmResource realm = realm();
            RealmRepresentation realmRepresentation = realm.toRepresentation();
            String realmId = requireText(realmRepresentation.getId(), "Keycloak realm id");

            ComponentRepresentation existing = findProvider(realm, realmId);
            if (existing == null) {
                return createProvider(realm, realmId);
            }

            convergeProvider(existing, realmId);
            realm.components().component(
                    requireText(existing.getId(), "Keycloak LDAP federation component id")
            ).update(existing);

            return existing.getId();
        } catch (ProcessingException exception) {
            throw unableToConnect(exception);
        } catch (WebApplicationException exception) {
            throw new KeycloakIntegrationException(
                    "Unable to configure Keycloak LDAP federation. HTTP "
                            + exception.getResponse().getStatus(),
                    exception
            );
        } catch (RuntimeException exception) {
            if (exception instanceof KeycloakIntegrationException integrationException) {
                throw integrationException;
            }
            throw new KeycloakIntegrationException(
                    "Unable to configure Keycloak LDAP federation",
                    exception
            );
        }
    }

    /**
     * Returns the configured LDAP provider id. If the provider is missing, it
     * is created before returning. This keeps account reconciliation resilient
     * when Keycloak was restored or the realm was recreated.
     */
    public String requireProviderId() {
        requireFederationEnabled();
        try {
            RealmResource realm = realm();
            String realmId = requireText(
                    realm.toRepresentation().getId(),
                    "Keycloak realm id"
            );
            ComponentRepresentation existing = findProvider(realm, realmId);
            if (existing != null) {
                return requireText(
                        existing.getId(),
                        "Keycloak LDAP federation component id"
                );
            }
        } catch (ProcessingException exception) {
            throw unableToConnect(exception);
        } catch (WebApplicationException exception) {
            throw new KeycloakIntegrationException(
                    "Unable to resolve Keycloak LDAP federation. HTTP "
                            + exception.getResponse().getStatus(),
                    exception
            );
        }

        return ensureConfigured();
    }

    private String createProvider(
            RealmResource realm,
            String realmId
    ) {
        ComponentRepresentation component = new ComponentRepresentation();
        component.setName(providerName());
        component.setProviderId(LDAP_PROVIDER_ID);
        component.setProviderType(USER_STORAGE_PROVIDER_TYPE);
        component.setParentId(realmId);
        component.setConfig(desiredConfig());

        try (Response response = realm.components().add(component)) {
            int status = response.getStatus();
            if (status != HTTP_CREATED) {
                throw new KeycloakIntegrationException(
                        "Unable to create Keycloak LDAP federation provider. HTTP " + status
                );
            }

            String createdId = normalizeNullable(CreatedResponseUtil.getCreatedId(response));
            if (createdId != null) {
                return createdId;
            }
        }

        ComponentRepresentation created = findProvider(realm, realmId);
        if (created == null) {
            throw new KeycloakIntegrationException(
                    "Keycloak LDAP federation provider was created but cannot be resolved"
            );
        }
        return requireText(created.getId(), "Keycloak LDAP federation component id");
    }

    private void convergeProvider(
            ComponentRepresentation component,
            String realmId
    ) {
        if (!LDAP_PROVIDER_ID.equals(component.getProviderId())
                || !USER_STORAGE_PROVIDER_TYPE.equals(component.getProviderType())) {
            throw new KeycloakIntegrationException(
                    "Keycloak component name is already used by a non-LDAP user-storage provider: "
                            + providerName()
            );
        }

        component.setName(providerName());
        component.setProviderId(LDAP_PROVIDER_ID);
        component.setProviderType(USER_STORAGE_PROVIDER_TYPE);
        component.setParentId(realmId);

        MultivaluedHashMap<String, String> config = component.getConfig();
        if (config == null) {
            config = new MultivaluedHashMap<>();
            component.setConfig(config);
        }

        MultivaluedHashMap<String, String> desired = desiredConfig();
        MultivaluedHashMap<String, String> targetConfig = config;
        desired.forEach((key, values) -> targetConfig.put(key, List.copyOf(values)));
    }

    private ComponentRepresentation findProvider(
            RealmResource realm,
            String realmId
    ) {
        ComponentsResource components = realm.components();
        List<ComponentRepresentation> matches = components.query(
                realmId,
                USER_STORAGE_PROVIDER_TYPE,
                providerName()
        );

        if (matches == null || matches.isEmpty()) {
            return null;
        }

        List<ComponentRepresentation> exactMatches = matches.stream()
                .filter(component -> Objects.equals(component.getName(), providerName()))
                .toList();

        if (exactMatches.size() > 1) {
            throw new KeycloakIntegrationException(
                    "Multiple Keycloak user-storage providers share the configured name: "
                            + providerName()
            );
        }

        return exactMatches.isEmpty() ? null : exactMatches.getFirst();
    }

    private MultivaluedHashMap<String, String> desiredConfig() {
        MultivaluedHashMap<String, String> config = new MultivaluedHashMap<>();

        put(config, "enabled", "true");
        put(config, "priority", "0");
        put(config, "fullSyncPeriod", "-1");
        put(config, "changedSyncPeriod", "-1");
        put(config, "cachePolicy", "DEFAULT");
        put(config, "batchSizeForSync", "1000");

        // 389 DS owns credentials directly. WRITABLE remains intentional so
        // Keycloak's UPDATE_PASSWORD UX can write a user-selected permanent
        // password back through the LDAP federation to the directory.
        put(config, "editMode", editMode());
        put(config, "syncRegistrations", "false");
        put(config, "importEnabled", "true");

        put(config, "vendor", "other");
        put(config, "usernameLDAPAttribute", "uid");
        put(config, "rdnLDAPAttribute", "uid");
        put(config, "uuidLDAPAttribute", "nsUniqueId");
        put(config, "userObjectClasses", "inetOrgPerson, organizationalPerson");

        // This URL is evaluated by the Keycloak runtime, not by Identity
        // Service. In the current dev topology Keycloak runs in a container,
        // therefore it may differ from integration.ds389.url.
        put(config, "connectionUrl", requireText(
                federationProperties.connectionUrl(),
                "integration.keycloak.ldap-federation.connection-url"
        ));
        put(config, "usersDn", usersDn());
        put(config, "authType", "simple");
        put(config, "bindDn", requireText(
                ds389Properties.bindDn(),
                "integration.ds389.bind-dn"
        ));
        put(config, "bindCredential", requireText(
                ds389Properties.bindPassword(),
                "integration.ds389.bind-password"
        ));
        put(config, "searchScope", "1");
        put(config, "connectionPooling", "true");
        put(config, "pagination", "true");
        put(config, "allowKerberosAuthentication", "false");
        put(config, "useKerberosForPasswordAuthentication", "false");
        put(config, "validatePasswordPolicy", "false");

        return config;
    }


    private void requireFederationEnabled() {
        if (!federationProperties.enabled()) {
            throw new KeycloakIntegrationException(
                    "Keycloak LDAP federation is disabled by integration.keycloak.ldap-federation.enabled"
            );
        }
    }

    private RealmResource realm() {
        return keycloakAdminClient.realm(
                requireText(keycloakProperties.realm(), "integration.keycloak.realm")
        );
    }

    private String usersDn() {
        String peopleOu = requireText(
                ds389Properties.peopleOu(),
                "integration.ds389.people-ou"
        );
        String baseDn = requireText(
                ds389Properties.baseDn(),
                "integration.ds389.base-dn"
        );

        if (peopleOu.toLowerCase(Locale.ROOT)
                .endsWith(baseDn.toLowerCase(Locale.ROOT))) {
            return peopleOu;
        }
        return peopleOu + "," + baseDn;
    }

    private String providerName() {
        return requireText(
                federationProperties.name(),
                "integration.keycloak.ldap-federation.name"
        );
    }

    private String editMode() {
        String mode = requireText(
                federationProperties.editMode(),
                "integration.keycloak.ldap-federation.edit-mode"
        ).toUpperCase(Locale.ROOT);

        if (!mode.equals("READ_ONLY")
                && !mode.equals("WRITABLE")
                && !mode.equals("UNSYNCED")) {
            throw new KeycloakIntegrationException(
                    "Unsupported Keycloak LDAP edit mode: " + mode
            );
        }
        return mode;
    }

    private static void put(
            MultivaluedHashMap<String, String> config,
            String key,
            String value
    ) {
        config.putSingle(key, value);
    }

    private static KeycloakIntegrationException unableToConnect(
            ProcessingException exception
    ) {
        return new KeycloakIntegrationException(
                "Unable to connect to Keycloak Admin API",
                exception
        );
    }

    private static String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
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