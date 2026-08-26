package com.hanyang.identity.identityservicev4mono.account.application.credential;


import com.hanyang.identity.identityservicev4mono.account.application.exception.AccountCredentialEmailUnavailableException;
import com.hanyang.identity.identityservicev4mono.account.application.exception.AccountCredentialOnboardingNotAllowedException;
import com.hanyang.identity.identityservicev4mono.account.application.exception.AccountNotFoundException;
import com.hanyang.identity.identityservicev4mono.account.application.port.IdentityProviderCredentialPort;
import com.hanyang.identity.identityservicev4mono.account.domain.Account;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountId;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountRepository;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountStatus;
import com.hanyang.identity.identityservicev4mono.security.authorization.IdentityAdminAccess;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@IdentityAdminAccess
@Service
@RequiredArgsConstructor
public class AccountCredentialOnboardingService {

    private final AccountRepository accountRepository;
    private final IdentityProviderCredentialPort credentialPort;
    private final TemporaryPasswordGenerator temporaryPasswordGenerator;

    /**
     * Generates a one-time temporary password and immediately installs it in
     * the external identity provider. The value is never persisted locally.
     */
    public TemporaryPasswordOnboardingResult issueTemporaryPassword(
            AccountId accountId
    ) {
        Account account = requireProvisionedActiveAccount(accountId);

        String temporaryPassword = temporaryPasswordGenerator.generate();

        credentialPort.setTemporaryPassword(
                account.getKeycloakSubject(),
                temporaryPassword
        );

        return new TemporaryPasswordOnboardingResult(
                temporaryPassword
        );
    }

    /**
     * Preferred onboarding path when the Keycloak identity has an email.
     * Keycloak sends an UPDATE_PASSWORD action so the permanent password never
     * passes through this service.
     */
    public void sendPasswordSetupEmail(AccountId accountId) {
        Account account = requireProvisionedActiveAccount(accountId);

        boolean requested = credentialPort.sendPasswordSetupEmail(
                account.getKeycloakSubject()
        );

        if (!requested) {
            throw new AccountCredentialEmailUnavailableException(accountId);
        }
    }

    private Account requireProvisionedActiveAccount(AccountId accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new AccountNotFoundException(accountId));

        if (account.getStatus() != AccountStatus.ACTIVE
                || account.getKeycloakSubject() == null
                || account.getKeycloakSubject().isBlank()) {
            throw new AccountCredentialOnboardingNotAllowedException(
                    accountId,
                    account.getStatus()
            );
        }

        return account;
    }
}