package com.hanyang.identity.identityservicev4mono.infrastructure.ldap.ds389.account;


import com.hanyang.identity.identityservicev4mono.account.application.port.DirectoryCredentialPort;
import com.hanyang.identity.identityservicev4mono.infrastructure.ldap.ds389.config.Ds389Properties;
import com.hanyang.identity.identityservicev4mono.infrastructure.ldap.ds389.exception.Ds389IntegrationException;
import org.springframework.ldap.NameNotFoundException;
import org.springframework.ldap.NamingException;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.support.LdapNameBuilder;

import javax.naming.Name;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.DirContext;
import javax.naming.directory.ModificationItem;
import java.util.Objects;

/**
 * 389 Directory Server credential adapter.
 *
 * <p>The raw value is sent only over LDAP/LDAPS to the directory. 389 DS is
 * responsible for applying its configured password storage scheme and storing
 * only the resulting password representation.</p>
 */
public class Ds389CredentialAdapter
        implements DirectoryCredentialPort {

    private static final String ATTR_UID = "uid";
    private static final String ATTR_USER_PASSWORD = "userPassword";

    private final LdapTemplate ldapTemplate;
    private final Ds389Properties properties;

    public Ds389CredentialAdapter(
            LdapTemplate ldapTemplate,
            Ds389Properties properties
    ) {
        this.ldapTemplate = Objects.requireNonNull(ldapTemplate, "ldapTemplate must not be null");
        this.properties = Objects.requireNonNull(properties, "properties must not be null");
    }

    @Override
    public void setPassword(
            String username,
            String rawPassword
    ) {
        String normalizedUsername = requireText(username, "username");
        String password = requirePassword(rawPassword);
        Name relativeDn = accountDn(normalizedUsername);

        ModificationItem[] modifications = {
                new ModificationItem(
                        DirContext.REPLACE_ATTRIBUTE,
                        new BasicAttribute(ATTR_USER_PASSWORD, password)
                )
        };

        try {
            ldapTemplate.modifyAttributes(relativeDn, modifications);
        } catch (NameNotFoundException exception) {
            throw new Ds389IntegrationException(
                    "389 DS account not found while setting password: "
                            + absoluteDn(relativeDn),
                    exception
            );
        } catch (NamingException exception) {
            throw new Ds389IntegrationException(
                    "Unable to set 389 DS password for account: "
                            + normalizedUsername,
                    exception
            );
        } catch (RuntimeException exception) {
            if (exception instanceof Ds389IntegrationException integrationException) {
                throw integrationException;
            }
            throw new Ds389IntegrationException(
                    "Unable to set 389 DS password for account: "
                            + normalizedUsername,
                    exception
            );
        }
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

    private static String requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value.trim();
    }

    private static String requirePassword(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("rawPassword must not be blank");
        }
        // Passwords are opaque secrets. Never trim or otherwise normalize them.
        return value;
    }
}