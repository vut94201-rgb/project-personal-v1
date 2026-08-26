package com.hanyang.identity.identityservicev4mono.account.application.credential;


import com.hanyang.identity.identityservicev4mono.account.application.exception.AccountCredentialEmailUnavailableException;
import com.hanyang.identity.identityservicev4mono.account.application.exception.AccountCredentialOnboardingNotAllowedException;
import com.hanyang.identity.identityservicev4mono.account.application.exception.AccountNotFoundException;
import com.hanyang.identity.identityservicev4mono.account.application.port.DirectoryCredentialPort;
import com.hanyang.identity.identityservicev4mono.account.application.port.IdentityProviderCredentialActionPort;

import com.hanyang.identity.identityservicev4mono.account.application.provisioning.AccountProvisioningState;
import com.hanyang.identity.identityservicev4mono.account.application.provisioning.AccountProvisioningStateRepository;
import com.hanyang.identity.identityservicev4mono.account.application.provisioning.AccountProvisioningStatus;
import com.hanyang.identity.identityservicev4mono.account.domain.Account;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountId;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountRepository;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountStatus;
import com.hanyang.identity.identityservicev4mono.security.authorization.IdentityAdminAccess;
import com.hanyang.identity.identityservicev4mono.shared.identityprovider.IdentityProviderType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@IdentityAdminAccess
@Service
@RequiredArgsConstructor
public class AccountCredentialOnboardingService {

    private static final IdentityProviderType PROVIDER = IdentityProviderType.KEYCLOAK;

    private final AccountRepository accountRepository;
    private final AccountProvisioningStateRepository provisioningStateRepository;
    private final DirectoryCredentialPort directoryCredentialPort;
    private final IdentityProviderCredentialActionPort credentialActionPort;
    private final TemporaryPasswordGenerator temporaryPasswordGenerator;

    /**
     * Generates a one-time temporary password, installs it directly in the
     * directory credential store, and tells the identity provider to require a
     * password change through its authentication UX.
     *
     * <p>The password value is never persisted locally. 389 DS owns the stored
     * password hash; Keycloak owns only the UPDATE_PASSWORD required action.</p>
     */
    public TemporaryPasswordOnboardingResult issueTemporaryPassword(
            AccountId accountId
    ) {
        ProvisionedAccount provisioned = requireProvisionedActiveAccount(accountId);
        String temporaryPassword = temporaryPasswordGenerator.generate();

        // Set the required action first. If the LDAP write then fails, the
        // account remains protected by the existing credential plus the
        // password-change requirement. The required action itself is idempotent.
        credentialActionPort.requirePasswordChange(
                provisioned.keycloakExternalId()
        );

        directoryCredentialPort.setPassword(
                provisioned.account().getUsername(),
                temporaryPassword
        );

        return new TemporaryPasswordOnboardingResult(
                temporaryPassword
        );
    }

    /**
     * Keycloak sends an UPDATE_PASSWORD action email. With the LDAP federation
     * in WRITABLE mode, the permanent password selected in that flow is written
     * back to 389 DS, which remains the credential source of truth.
     */
    public void sendPasswordSetupEmail(AccountId accountId) {
        ProvisionedAccount provisioned = requireProvisionedActiveAccount(accountId);

        boolean requested = credentialActionPort.sendPasswordSetupEmail(
                provisioned.keycloakExternalId()
        );

        if (!requested) {
            throw new AccountCredentialEmailUnavailableException(accountId);
        }
    }

    private ProvisionedAccount requireProvisionedActiveAccount(AccountId accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountCredentialOnboardingNotAllowedException(
                    accountId,
                    account.getStatus()
            );
        }

        AccountProvisioningState state = provisioningStateRepository
                .findByAccountIdAndProvider(accountId, PROVIDER)
                .filter(AccountCredentialOnboardingService::isCurrent)
                .filter(AccountCredentialOnboardingService::hasExternalId)
                .orElseThrow(() -> new AccountCredentialOnboardingNotAllowedException(
                        accountId,
                        account.getStatus()
                ));

        return new ProvisionedAccount(account, state.getExternalId());
    }

    private static boolean isCurrent(AccountProvisioningState state) {
        return state.getStatus() == AccountProvisioningStatus.SYNCED
                && state.getSyncedRevision() == state.getDesiredRevision();
    }

    private static boolean hasExternalId(AccountProvisioningState state) {
        return state.getExternalId() != null && !state.getExternalId().isBlank();
    }

    private record ProvisionedAccount(
            Account account,
            String keycloakExternalId
    ) {
    }
}