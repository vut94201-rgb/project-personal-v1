package com.hanyang.identity.identityservicev4mono.account.application.provisioning;


import com.hanyang.identity.identityservicev4mono.account.application.activation.AccountActivationCoordinator;
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

    public static final String OUTBOX_AGGREGATE_TYPE = "ACCOUNT";
    public static final String OUTBOX_EVENT_TYPE = "ACCOUNT_PROVISIONING_REQUESTED";

    private static final IdentityProviderType PROVIDER = IdentityProviderType.KEYCLOAK;

    private final AccountRepository accountRepository;
    private final AccountProvisioningStateRepository provisioningStateRepository;
    private final IdentityProviderAccountPort identityProviderAccountPort;
    private final AccountActivationCoordinator activationCoordinator;
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

        AccountProvisioningState synchronizedState;
        try {
            AccountStatus attemptedStatus = account.getStatus();
            IdentityProviderAccountPort.ProvisionedAccount provisionedAccount =
                    synchronizeExternalAccount(account, syncingState.getExternalId());

            Account latestAccount = accountRepository.findById(accountId)
                    .orElseThrow(() -> new IllegalStateException(
                            "Account disappeared during reconciliation: " + accountId.value()
                    ));

            // The Keycloak external id is persisted only in the provisioning
            // binding. The Account aggregate remains provider-neutral.
            //
            // Reconcile only if the effective business state changed while the
            // remote call was in flight (for example ACTIVE -> DISABLED).
            if (authenticationAllowed(attemptedStatus)
                    != authenticationAllowed(latestAccount.getStatus())) {
                provisionedAccount = synchronizeExternalAccount(
                        latestAccount,
                        provisionedAccount.externalId()
                );
            }

            Instant synchronizedAt = clock.instant();
            synchronizedState = provisioningStateRepository
                    .completeSynchronization(
                            accountId,
                            PROVIDER,
                            synchronizedRevision,
                            provisionedAccount.externalId(),
                            provisionedAccount.externalCode(),
                            synchronizedAt
                    );
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

        // The provider result is already durably SYNCED. Coordination is a
        // separate step: if it fails, the outbox can retry without corrupting
        // the successful provider state.
        activationCoordinator.afterIdentityProviderSynchronization(accountId);
        return AccountReconciliationResult.fromState(synchronizedState);
    }

    private IdentityProviderAccountPort.ProvisionedAccount synchronizeExternalAccount(
            Account account,
            String externalId
    ) {
        if (account.getStatus() == AccountStatus.DISABLED) {
            return identityProviderAccountPort.disableAccount(
                    account.getUsername(),
                    externalId
            );
        }

        return identityProviderAccountPort.ensureAccount(
                account.getUsername(),
                externalId,
                authenticationAllowed(account.getStatus())
        );
    }

    private static boolean authenticationAllowed(AccountStatus status) {
        return status == AccountStatus.ACTIVE;
    }

    private static String messageOf(RuntimeException exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank()
                ? exception.getClass().getSimpleName()
                : message;
    }
}