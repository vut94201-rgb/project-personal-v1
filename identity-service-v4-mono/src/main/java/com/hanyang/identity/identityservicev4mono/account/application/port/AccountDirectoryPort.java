package com.hanyang.identity.identityservicev4mono.account.application.port;


import java.util.Optional;

/**
 * Provider-neutral port for an account directory such as 389 Directory Server.
 *
 * <p>This port deliberately models directory concerns separately from
 * {@link IdentityProviderAccountPort}. A directory owns LDAP identity and
 * credential enforcement, while an identity provider owns OIDC/OAuth2-facing
 * identity, sessions and tokens.</p>
 */
public interface AccountDirectoryPort {

    DirectoryAccount ensureAccount(DirectoryAccountSpec spec);

    DirectoryAccount setAuthenticationAllowed(
            String username,
            boolean authenticationAllowed
    );

    Optional<DirectoryAccount> findByUsername(String username);

    record DirectoryAccountSpec(
            String username,
            String employeeNumber,
            String commonName,
            String surname,
            String email,
            boolean authenticationAllowed
    ) {
    }

    record DirectoryAccount(
            String username,
            String externalDn,
            boolean authenticationAllowed
    ) {
    }
}