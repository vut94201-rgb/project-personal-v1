package com.hanyang.identity.identityservicev4mono.account.application.credential;



import com.hanyang.identity.identityservicev4mono.account.application.exception.AccountCredentialOnboardingNotAllowedException;
import com.hanyang.identity.identityservicev4mono.account.application.exception.AccountNotFoundException;
import com.hanyang.identity.identityservicev4mono.account.application.port.DirectoryCredentialPort;


import com.hanyang.identity.identityservicev4mono.account.application.port.IdentityProviderCredentialPolicyPort;
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
    private final IdentityProviderCredentialPolicyPort credentialPolicyPort;
    private final InitialPasswordGenerator initialPasswordGenerator;

    public InitialPasswordOnboardingResult issueInitialPassword(
            AccountId accountId
    ) {
        ProvisionedAccount provisioned = requireProvisionedActiveAccount(accountId);
        String initialPassword = initialPasswordGenerator.generate();

        // Older versions of Hanyang installed UPDATE_PASSWORD here. Remove it
        // first so both existing and newly onboarded users follow the current
        // business rule: changing the generated password is optional.
        credentialPolicyPort.clearPasswordChangeRequirement(
                provisioned.keycloakExternalId()
        );

        directoryCredentialPort.setPassword(
                provisioned.account().getUsername(),
                initialPassword
        );

        return new InitialPasswordOnboardingResult(
                initialPassword
        );
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

        return new ProvisionedAccount(
                account,
                state.getExternalId()
        );
    }

    private static boolean isCurrent(AccountProvisioningState state) {
        return state.getStatus() == AccountProvisioningStatus.SYNCED
                && state.getSyncedRevision() == state.getDesiredRevision();
    }

    private static boolean hasExternalId(AccountProvisioningState state) {
        return state.getExternalId() != null
                && !state.getExternalId().isBlank();
    }

    private record ProvisionedAccount(
            Account account,
            String keycloakExternalId
    ) {
    }
}