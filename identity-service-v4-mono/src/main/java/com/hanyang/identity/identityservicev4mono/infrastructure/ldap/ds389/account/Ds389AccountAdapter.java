package com.hanyang.identity.identityservicev4mono.infrastructure.ldap.ds389.account;


import com.hanyang.identity.identityservicev4mono.account.application.port.AccountDirectoryPort;
import com.hanyang.identity.identityservicev4mono.infrastructure.ldap.ds389.config.Ds389Properties;
import com.hanyang.identity.identityservicev4mono.infrastructure.ldap.ds389.exception.Ds389IntegrationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ldap.NameAlreadyBoundException;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.NamingException;
import org.springframework.ldap.core.ContextMapper;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.support.LdapNameBuilder;

import javax.naming.Name;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

/**
 * 389 Directory Server adapter for account-directory operations.
 *
 * <p>All DNs supplied to {@link LdapTemplate} are relative to the configured
 * base DN. The externally returned DN is absolute so it can later be persisted
 * as a stable directory binding.</p>
 */
@Slf4j
public class Ds389AccountAdapter implements AccountDirectoryPort {

    private static final String ATTR_UID = "uid";
    private static final String ATTR_CN = "cn";
    private static final String ATTR_SN = "sn";
    private static final String ATTR_EMPLOYEE_NUMBER = "employeeNumber";
    private static final String ATTR_MAIL = "mail";
    private static final String ATTR_ACCOUNT_LOCK = "nsAccountLock";

    private final LdapTemplate ldapTemplate;
    private final Ds389Properties properties;

    public Ds389AccountAdapter(
            LdapTemplate ldapTemplate,
            Ds389Properties properties
    ) {
        this.ldapTemplate = Objects.requireNonNull(
                ldapTemplate,
                "ldapTemplate must not be null"
        );
        this.properties = Objects.requireNonNull(
                properties,
                "properties must not be null"
        );
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
                    /*
                     * Another worker created the entry between lookup and bind.
                     * Treat the operation as idempotent and converge it.
                     */
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
                    "Unable to synchronize 389 DS account: "
                            + normalized.username(),
                    exception
            );

        } catch (RuntimeException exception) {
            if (exception instanceof Ds389IntegrationException integrationException) {
                throw integrationException;
            }

            throw new Ds389IntegrationException(
                    "Unable to synchronize 389 DS account: "
                            + normalized.username(),
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
            requireExisting(relativeDn);

            if (authenticationAllowed) {
                removeAccountLock(relativeDn);
            } else {
                lockAccount(relativeDn);
            }

            return toDirectoryAccount(
                    normalizedUsername,
                    authenticationAllowed
            );

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

            /*
             * nsAccountLock is handled separately because it is an operational
             * attribute and may not be returned by a normal lookupContext().
             *
             * If legacy/broken data contains both true and false, ANY true
             * value means we conservatively treat the account as locked.
             */
            String[] lockValues = lookupAccountLockValues(relativeDn);

            boolean locked = Arrays.stream(lockValues)
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .anyMatch(value -> Boolean.TRUE.toString().equalsIgnoreCase(value));

            return Optional.of(
                    toDirectoryAccount(
                            normalizedUsername,
                            !locked
                    )
            );

        } catch (NamingException exception) {
            throw new Ds389IntegrationException(
                    "Unable to read 389 DS account: "
                            + normalizedUsername,
                    exception
            );
        }
    }

    /**
     * Creates the LDAP entry.
     *
     * <p>If authentication is not allowed at creation time, the account is
     * created locked. If authentication is allowed, nsAccountLock is omitted.</p>
     */
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

        if (!spec.authenticationAllowed()) {
            context.setAttributeValue(
                    ATTR_ACCOUNT_LOCK,
                    Boolean.TRUE.toString()
            );
        }

        ldapTemplate.bind(context);
    }

    /**
     * Converges normal LDAP profile attributes first, then converges the
     * authentication state separately.
     *
     * <p>lookupContext() is intentionally used for the normal attributes
     * because the resulting DirContextOperations instance is properly
     * initialized for ldapTemplate.modifyAttributes(context).</p>
     */
    private void update(
            DirContextOperations context,
            DirectoryAccountSpec spec
    ) {
        mapManagedAttributes(spec, context);
        ldapTemplate.modifyAttributes(context);

        Name relativeDn = accountDn(spec.username());

        if (spec.authenticationAllowed()) {
            removeAccountLock(relativeDn);
        } else {
            lockAccount(relativeDn);
        }
    }

    /**
     * Maps only normal account/profile attributes.
     *
     * <p>nsAccountLock MUST NOT be handled here.</p>
     */
    private void mapManagedAttributes(
            DirectoryAccountSpec spec,
            DirContextOperations context
    ) {
        context.setAttributeValue(
                ATTR_UID,
                spec.username()
        );

        context.setAttributeValue(
                ATTR_CN,
                spec.commonName()
        );

        context.setAttributeValue(
                ATTR_SN,
                spec.surname()
        );

        context.setAttributeValue(
                ATTR_EMPLOYEE_NUMBER,
                spec.employeeNumber()
        );

        if (spec.email() == null) {
            context.setAttributeValues(
                    ATTR_MAIL,
                    null
            );
        } else {
            context.setAttributeValue(
                    ATTR_MAIL,
                    spec.email()
            );
        }
    }

    /**
     * Normal entry lookup used when the returned context may later be modified
     * through ldapTemplate.modifyAttributes(context).
     */
    private DirContextOperations lookup(Name relativeDn) {
        try {
            return ldapTemplate.lookupContext(relativeDn);
        } catch (NameNotFoundException exception) {
            return null;
        }
    }

    private DirContextOperations requireExisting(Name relativeDn) {
        DirContextOperations context = lookup(relativeDn);

        if (context == null) {
            throw new Ds389IntegrationException(
                    "389 DS account not found: "
                            + absoluteDn(relativeDn)
            );
        }

        return context;
    }

    /**
     * Locks the account.
     *
     * <p>REPLACE_ATTRIBUTE also cleans up broken legacy state such as
     * nsAccountLock=true + nsAccountLock=false and converges it to one value:
     * nsAccountLock=true.</p>
     */
    private void lockAccount(Name relativeDn) {
        ModificationItem modification =
                new ModificationItem(
                        DirContext.REPLACE_ATTRIBUTE,
                        new BasicAttribute(
                                ATTR_ACCOUNT_LOCK,
                                Boolean.TRUE.toString()
                        )
                );

        ldapTemplate.modifyAttributes(
                relativeDn,
                new ModificationItem[]{modification}
        );
    }

    /**
     * Unlocks the account by removing nsAccountLock entirely.
     *
     * <p>This method is idempotent: if the attribute is already absent,
     * no LDAP modification is issued.</p>
     */
    private void removeAccountLock(Name relativeDn) {
        String[] values = lookupAccountLockValues(relativeDn);

        if (values.length == 0) {
            return;
        }

        ModificationItem modification =
                new ModificationItem(
                        DirContext.REMOVE_ATTRIBUTE,
                        new BasicAttribute(ATTR_ACCOUNT_LOCK)
                );

        ldapTemplate.modifyAttributes(
                relativeDn,
                new ModificationItem[]{modification}
        );
    }

    /**
     * Explicitly reads nsAccountLock because operational attributes are not
     * guaranteed to be returned by normal lookupContext().
     *
     * <p>This context is READ ONLY for our purposes; it is never passed to
     * ldapTemplate.modifyAttributes(context).</p>
     */
    private String[] lookupAccountLockValues(Name relativeDn) {
        try {
            return ldapTemplate.lookup(
                    relativeDn,
                    new String[]{ATTR_ACCOUNT_LOCK},
                    (ContextMapper<String[]>) context -> {
                        DirContextOperations operations =
                                (DirContextOperations) context;

                        String[] values =
                                operations.getStringAttributes(
                                        ATTR_ACCOUNT_LOCK
                                );

                        return values == null
                                ? new String[0]
                                : values;
                    }
            );

        } catch (NameNotFoundException exception) {
            throw new Ds389IntegrationException(
                    "389 DS account not found: "
                            + absoluteDn(relativeDn),
                    exception
            );
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
                .newInstance(
                        requireText(
                                properties.peopleOu(),
                                "integration.ds389.people-ou"
                        )
                )
                .add(
                        ATTR_UID,
                        username
                )
                .build();
    }

    private String absoluteDn(Name relativeDn) {
        return LdapNameBuilder
                .newInstance(
                        requireText(
                                properties.baseDn(),
                                "integration.ds389.base-dn"
                        )
                )
                .add(relativeDn)
                .build()
                .toString();
    }

    private static DirectoryAccountSpec normalize(
            DirectoryAccountSpec spec
    ) {
        String commonName = requireText(
                spec.commonName(),
                "commonName"
        );

        String surname = normalizeNullable(
                spec.surname()
        );

        return new DirectoryAccountSpec(
                requireText(
                        spec.username(),
                        "username"
                ),
                requireText(
                        spec.employeeNumber(),
                        "employeeNumber"
                ),
                commonName,
                surname == null
                        ? commonName
                        : surname,
                normalizeNullable(
                        spec.email()
                ),
                spec.authenticationAllowed()
        );
    }

    private static String requireText(
            String value,
            String fieldName
    ) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " must not be blank"
            );
        }

        return value.trim();
    }

    private static String normalizeNullable(String value) {
        if (value == null) {
            return null;
        }

        String normalized = value.trim();

        return normalized.isEmpty()
                ? null
                : normalized;
    }
}