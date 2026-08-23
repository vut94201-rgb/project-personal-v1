package com.hanyang.identity.identityservicev4mono.account.application.provisioning;


import com.hanyang.identity.identityservicev4mono.account.application.port.IdentityProviderAccountPort;
import com.hanyang.identity.identityservicev4mono.account.domain.Account;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountId;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountRepository;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountStatus;
import com.hanyang.identity.identityservicev4mono.shared.identityprovider.IdentityProviderType;
import com.hanyang.identity.identityservicev4mono.shared.outbox.OutboxPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class AccountProvisioningService {

    static final String OUTBOX_AGGREGATE_TYPE = "ACCOUNT";
    static final String OUTBOX_EVENT_TYPE = "ACCOUNT_PROVISIONING_REQUESTED";

    private static final IdentityProviderType PROVIDER = IdentityProviderType.KEYCLOAK;

    private final AccountRepository accountRepository;
    private final AccountProvisioningStateRepository provisioningStateRepository;
    private final IdentityProviderAccountPort identityProviderAccountPort;
    private final OutboxPublisher outboxPublisher;
    private final Clock clock;

    @Transactional
    public void requestSynchronization(AccountId accountId) {
        provisioningStateRepository.requestSynchronization(accountId, PROVIDER);
        outboxPublisher.publish(
                OUTBOX_AGGREGATE_TYPE,
                accountId.value().toString(),
                OUTBOX_EVENT_TYPE,
                null
        );
    }

    public AccountReconciliationResult reconcile(AccountId accountId) {
        Account account = accountRepository.findById(accountId).orElse(null);
        if (account == null) {
            return AccountReconciliationResult.failed(
                    accountId,
                    PROVIDER,
                    "Account not found: " + accountId.value()
            );
        }

        AccountProvisioningState syncingState = provisioningStateRepository
                .beginSynchronization(accountId, PROVIDER);
        long synchronizedRevision = syncingState.getDesiredRevision();

        try {
            boolean attemptedEnabledState = account.getStatus() != AccountStatus.DISABLED;
            IdentityProviderAccountPort.ProvisionedAccount provisionedAccount =
                    synchronizeExternalAccount(account);

            Account latestAccount = accountRepository.findById(accountId)
                    .orElseThrow(() -> new IllegalStateException(
                            "Account disappeared during reconciliation: " + accountId.value()
                    ));

            if (latestAccount.getStatus() != AccountStatus.DISABLED
                    && provisionedAccount.externalId() != null
                    && !provisionedAccount.externalId().isBlank()) {
                latestAccount.provision(provisionedAccount.externalId());
                accountRepository.save(latestAccount);
            } else if (attemptedEnabledState
                    && latestAccount.getStatus() == AccountStatus.DISABLED
                    && provisionedAccount.externalId() != null
                    && !provisionedAccount.externalId().isBlank()) {
                // A disable may have raced with an earlier provisioning attempt.
                // Compensate immediately so a stale attempt cannot reactivate access.
                provisionedAccount = identityProviderAccountPort.disableAccount(
                        latestAccount.getUsername(),
                        provisionedAccount.externalId()
                );
            }

            Instant synchronizedAt = clock.instant();
            AccountProvisioningState synchronizedState = provisioningStateRepository
                    .completeSynchronization(
                            accountId,
                            PROVIDER,
                            synchronizedRevision,
                            provisionedAccount.externalId(),
                            provisionedAccount.externalCode(),
                            synchronizedAt
                    );

            return AccountReconciliationResult.fromState(synchronizedState);
        } catch (RuntimeException exception) {
            AccountProvisioningState failedState = provisioningStateRepository
                    .failSynchronization(
                            accountId,
                            PROVIDER,
                            synchronizedRevision,
                            messageOf(exception)
                    );

            return AccountReconciliationResult.fromState(failedState);
        }
    }

    private IdentityProviderAccountPort.ProvisionedAccount synchronizeExternalAccount(
            Account account
    ) {
        if (account.getStatus() == AccountStatus.DISABLED) {
            return identityProviderAccountPort.disableAccount(
                    account.getUsername(),
                    account.getKeycloakSubject()
            );
        }

        return identityProviderAccountPort.ensureAccount(
                account.getUsername(),
                account.getKeycloakSubject(),
                true
        );
    }

    private static String messageOf(RuntimeException exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank()
                ? exception.getClass().getSimpleName()
                : message;
    }
}