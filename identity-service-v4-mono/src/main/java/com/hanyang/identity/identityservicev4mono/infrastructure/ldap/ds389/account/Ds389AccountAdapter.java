package com.hanyang.identity.identityservicev4mono.infrastructure.ldap.ds389.account;


import com.hanyang.identity.identityservicev4mono.account.application.port.AccountDirectoryPort;
import com.hanyang.identity.identityservicev4mono.infrastructure.ldap.ds389.config.Ds389Properties;
import com.hanyang.identity.identityservicev4mono.infrastructure.ldap.ds389.exception.Ds389IntegrationException;
import org.springframework.ldap.NameAlreadyBoundException;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.NamingException;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.support.LdapNameBuilder;

import javax.naming.Name;
import java.util.Objects;
import java.util.Optional;

/**
 * 389 Directory Server adapter for account-directory operations.
 *
 * <p>All DNs supplied to {@link LdapTemplate} are relative to the configured
 * base DN. The externally returned DN is absolute so it can later be persisted
 * as a stable directory binding.</p>
 */
public class Ds389AccountAdapter implements AccountDirectoryPort {

    private static final String ATTR_UID = "uid";
    private static final String ATTR_CN = "cn";
    private static final String ATTR_SN = "sn";
    private static final String ATTR_EMPLOYEE_NUMBER = "employeeNumber";
    private static final String ATTR_MAIL = "mail";
    private static final String ATTR_ACCOUNT_LOCK = "nsAccountLock";

    private static final String[] MANAGED_ATTRIBUTES = {
            ATTR_UID,
            ATTR_CN,
            ATTR_SN,
            ATTR_EMPLOYEE_NUMBER,
            ATTR_MAIL,
            ATTR_ACCOUNT_LOCK
    };

    private final LdapTemplate ldapTemplate;
    private final Ds389Properties properties;

    public Ds389AccountAdapter(
            LdapTemplate ldapTemplate,
            Ds389Properties properties
    ) {
        this.ldapTemplate = Objects.requireNonNull(ldapTemplate, "ldapTemplate must not be null");
        this.properties = Objects.requireNonNull(properties, "properties must not be null");
    }

    @Override
    public DirectoryAccount ensureAccount(DirectoryAccountSpec spec) {
        Objects.requireNonNull(spec, "spec must not be null");

        DirectoryAccountSpec normalized = normalize(spec);
        Name relativeDn = accountDn(normalized.username());

        try {
            DirContextOperations existing = lookup(relativeDn);
            if (existing == null) {
                try {
                    create(relativeDn, normalized);
                } catch (NameAlreadyBoundException race) {
                    // Another provisioning attempt won the create race. Treat it as
                    // an idempotent ensure and converge the existing entry instead.
                    update(requireExisting(relativeDn), normalized);
                }
            } else {
                update(existing, normalized);
            }

            return toDirectoryAccount(
                    normalized.username(),
                    normalized.authenticationAllowed()
            );
        } catch (NamingException exception) {
            throw new Ds389IntegrationException(
                    "Unable to synchronize 389 DS account: " + normalized.username(),
                    exception
            );
        } catch (RuntimeException exception) {
            if (exception instanceof Ds389IntegrationException integrationException) {
                throw integrationException;
            }
            throw new Ds389IntegrationException(
                    "Unable to synchronize 389 DS account: " + normalized.username(),
                    exception
            );
        }
    }

    @Override
    public DirectoryAccount setAuthenticationAllowed(
            String username,
            boolean authenticationAllowed
    ) {
        String normalizedUsername = requireText(username, "username");
        Name relativeDn = accountDn(normalizedUsername);

        try {
            DirContextOperations context = requireExisting(relativeDn);
            context.setAttributeValue(
                    ATTR_ACCOUNT_LOCK,
                    Boolean.toString(!authenticationAllowed)
            );
            ldapTemplate.modifyAttributes(context);

            return toDirectoryAccount(normalizedUsername, authenticationAllowed);
        } catch (NamingException exception) {
            throw new Ds389IntegrationException(
                    "Unable to change 389 DS authentication state for account: "
                            + normalizedUsername,
                    exception
            );
        }
    }

    @Override
    public Optional<DirectoryAccount> findByUsername(String username) {
        String normalizedUsername = requireText(username, "username");
        Name relativeDn = accountDn(normalizedUsername);

        try {
            DirContextOperations context = lookup(relativeDn);
            if (context == null) {
                return Optional.empty();
            }

            boolean locked = Boolean.parseBoolean(
                    normalizeNullable(context.getStringAttribute(ATTR_ACCOUNT_LOCK))
            );

            return Optional.of(toDirectoryAccount(
                    normalizedUsername,
                    !locked
            ));
        } catch (NamingException exception) {
            throw new Ds389IntegrationException(
                    "Unable to read 389 DS account: " + normalizedUsername,
                    exception
            );
        }
    }

    private void create(
            Name relativeDn,
            DirectoryAccountSpec spec
    ) {
        DirContextAdapter context = new DirContextAdapter(relativeDn);
        context.setAttributeValues(
                "objectClass",
                new String[]{
                        "top",
                        "person",
                        "organizationalPerson",
                        "inetOrgPerson"
                }
        );
        mapManagedAttributes(spec, context);
        ldapTemplate.bind(context);
    }

    private void update(
            DirContextOperations context,
            DirectoryAccountSpec spec
    ) {
        mapManagedAttributes(spec, context);
        ldapTemplate.modifyAttributes(context);
    }

    private void mapManagedAttributes(
            DirectoryAccountSpec spec,
            DirContextOperations context
    ) {
        context.setAttributeValue(ATTR_UID, spec.username());
        context.setAttributeValue(ATTR_CN, spec.commonName());
        context.setAttributeValue(ATTR_SN, spec.surname());
        context.setAttributeValue(ATTR_EMPLOYEE_NUMBER, spec.employeeNumber());
        context.setAttributeValue(
                ATTR_ACCOUNT_LOCK,
                Boolean.toString(!spec.authenticationAllowed())
        );

        if (spec.email() == null) {
            context.setAttributeValues(ATTR_MAIL, null);
        } else {
            context.setAttributeValue(ATTR_MAIL, spec.email());
        }
    }

    private DirContextOperations requireExisting(Name relativeDn) {
        DirContextOperations context = lookup(relativeDn);
        if (context == null) {
            throw new Ds389IntegrationException(
                    "389 DS account not found: " + absoluteDn(relativeDn)
            );
        }
        return context;
    }

    private DirContextOperations lookup(Name relativeDn) {
        try {
            return ldapTemplate.lookup(
                    relativeDn,
                    MANAGED_ATTRIBUTES,
                    (ContextMapper<DirContextOperations>) context ->
                            (DirContextOperations) context
            );
        } catch (NameNotFoundException exception) {
            return null;
        }
    }

    private DirectoryAccount toDirectoryAccount(
            String username,
            boolean authenticationAllowed
    ) {
        Name relativeDn = accountDn(username);
        return new DirectoryAccount(
                username,
                absoluteDn(relativeDn),
                authenticationAllowed
        );
    }

    private Name accountDn(String username) {
        return LdapNameBuilder
                .newInstance(requireText(properties.peopleOu(), "integration.ds389.people-ou"))
                .add(ATTR_UID, username)
                .build();
    }

    private String absoluteDn(Name relativeDn) {
        return LdapNameBuilder
                .newInstance(requireText(properties.baseDn(), "integration.ds389.base-dn"))
                .add(relativeDn)
                .build()
                .toString();
    }

    private static AccountDirectoryPort.DirectoryAccountSpec normalize(DirectoryAccountSpec spec) {
        String commonName = requireText(spec.commonName(), "commonName");
        String surname = normalizeNullable(spec.surname());

        return new DirectoryAccountSpec(
                requireText(spec.username(), "username"),
                requireText(spec.employeeNumber(), "employeeNumber"),
                commonName,
                surname == null ? commonName : surname,
                normalizeNullable(spec.email()),
                spec.authenticationAllowed()
        );
    }

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }

    private static String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }
        String normalized = value.trim();
        return normalized.isEmpty() ? null : normalized;
    }
}