package com.hanyang.identity.identityservicev4mono.access.application.provisioning;


import com.hanyang.identity.identityservicev4mono.access.application.port.IdentityProviderAccessPort;
import com.hanyang.identity.identityservicev4mono.access.domain.*;
import com.hanyang.identity.identityservicev4mono.account.application.provisioning.AccountProvisioningService;
import com.hanyang.identity.identityservicev4mono.account.application.provisioning.AccountProvisioningState;
import com.hanyang.identity.identityservicev4mono.account.application.provisioning.AccountProvisioningStateRepository;
import com.hanyang.identity.identityservicev4mono.account.application.provisioning.AccountProvisioningStatus;
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
public class AccountRoleProvisioningService {

    static final String OUTBOX_AGGREGATE_TYPE = "ACCOUNT_ROLE";
    static final String OUTBOX_EVENT_TYPE = "ACCOUNT_ROLE_PROVISIONING_REQUESTED";

    private static final IdentityProviderType PROVIDER = IdentityProviderType.KEYCLOAK;

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final ApplicationRepository applicationRepository;
    private final AccountRoleRepository accountRoleRepository;
    private final AccountRoleProvisioningStateRepository provisioningStateRepository;
    private final AccountProvisioningStateRepository accountProvisioningStateRepository;
    private final RoleProvisioningService roleProvisioningService;
    private final RoleProvisioningStateRepository roleProvisioningStateRepository;
    private final IdentityProviderAccessPort identityProviderAccessPort;
    private final OutboxPublisher outboxPublisher;
    private final Clock clock;

    @Transactional
    public void requestSynchronization(
            AccountId accountId,
            RoleId roleId,
            boolean desiredAssigned
    ) {
        provisioningStateRepository.requestSynchronization(
                accountId,
                roleId,
                PROVIDER,
                desiredAssigned
        );

        outboxPublisher.publish(
                OUTBOX_AGGREGATE_TYPE,
                new AccountRoleProvisioningKey(accountId, roleId).serialize(),
                OUTBOX_EVENT_TYPE,
                null
        );
    }

    public AccountRoleReconciliationResult reconcile(
            AccountId accountId,
            RoleId roleId
    ) {
        AccountRoleProvisioningState syncingState;
        try {
            if (provisioningStateRepository
                    .findByKeyAndProvider(accountId, roleId, PROVIDER)
                    .isEmpty()) {
                provisioningStateRepository.requestSynchronization(
                        accountId,
                        roleId,
                        PROVIDER,
                        accountRoleRepository.exists(accountId, roleId)
                );
            }

            syncingState = provisioningStateRepository.beginSynchronization(
                    accountId,
                    roleId,
                    PROVIDER
            );
        } catch (RuntimeException exception) {
            return AccountRoleReconciliationResult.failed(
                    accountId,
                    roleId,
                    PROVIDER,
                    false,
                    messageOf(exception)
            );
        }

        long synchronizedRevision = syncingState.getDesiredRevision();
        boolean desiredAssigned = syncingState.isDesiredAssigned();

        try {
            Account account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new IllegalStateException(
                            "Account not found: " + accountId.value()
                    ));

            Role role = roleRepository.findById(roleId)
                    .orElseThrow(() -> new IllegalStateException(
                            "Role not found: " + roleId.value()
                    ));

            Application application = applicationRepository
                    .findById(role.getApplicationId())
                    .orElseThrow(() -> new IllegalStateException(
                            "Application not found for role "
                                    + roleId.value()
                                    + ": "
                                    + role.getApplicationId().value()
                    ));

            if (desiredAssigned) {
                String externalAccountId = requireCurrentExternalAccountId(account);
                ensureRoleCanBeAssigned(role, application);

                identityProviderAccessPort.assignRole(
                        externalAccountId,
                        application.getCode(),
                        role.getCode()
                );
            } else {
                currentExternalAccountId(account.getId()).ifPresent(externalAccountId ->
                        identityProviderAccessPort.revokeRole(
                                externalAccountId,
                                application.getCode(),
                                role.getCode()
                        )
                );
            }

            Instant synchronizedAt = clock.instant();
            AccountRoleProvisioningState synchronizedState = provisioningStateRepository
                    .completeSynchronization(
                            accountId,
                            roleId,
                            PROVIDER,
                            synchronizedRevision,
                            synchronizedAt
                    );

            return AccountRoleReconciliationResult.fromState(synchronizedState);
        } catch (RuntimeException exception) {
            AccountRoleProvisioningState failedState = provisioningStateRepository
                    .failSynchronization(
                            accountId,
                            roleId,
                            PROVIDER,
                            synchronizedRevision,
                            messageOf(exception)
                    );

            return AccountRoleReconciliationResult.fromState(failedState);
        }
    }

    private String requireCurrentExternalAccountId(Account account) {
        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Account must be ACTIVE before identity-provider role assignment: "
                            + account.getId().value()
            );
        }

        return currentExternalAccountId(account.getId())
                .orElseThrow(() -> new IllegalStateException(
                        "Account Keycloak binding is not current before role assignment: "
                                + account.getId().value()
                ));
    }

    private java.util.Optional<String> currentExternalAccountId(AccountId accountId) {
        return accountProvisioningStateRepository
                .findByAccountIdAndProvider(accountId, PROVIDER)
                .filter(AccountRoleProvisioningService::isCurrentAccountBinding)
                .map(AccountProvisioningState::getExternalId)
                .filter(externalId -> externalId != null && !externalId.isBlank());
    }

    private static boolean isCurrentAccountBinding(AccountProvisioningState state) {
        return state.getStatus() == AccountProvisioningStatus.SYNCED
                && state.getDesiredRevision() == state.getSyncedRevision();
    }

    private void ensureRoleCanBeAssigned(
            Role role,
            Application application
    ) {
        if (role.getStatus() != RoleStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Cannot assign disabled role: " + role.getId().value()
            );
        }

        if (application.getStatus() != ApplicationStatus.ACTIVE) {
            throw new IllegalStateException(
                    "Cannot assign role from disabled application: "
                            + application.getId().value()
            );
        }

        boolean synchronizedRole = roleProvisioningStateRepository
                .findByRoleIdAndProvider(role.getId(), PROVIDER)
                .filter(state -> state.getStatus() == RoleProvisioningStatus.SYNCED)
                .filter(state -> state.getDesiredRevision() == state.getSyncedRevision())
                .isPresent();

        if (synchronizedRole) {
            return;
        }

        RoleReconciliationResult result = roleProvisioningService.reconcile(role.getId());
        if (result.status() == RoleProvisioningStatus.SYNCED) {
            return;
        }

        throw new IllegalStateException(
                result.error() == null || result.error().isBlank()
                        ? "Role provisioning did not complete before account-role synchronization. "
                        + "roleId="
                        + role.getId().value()
                        + ", status="
                        + result.status()
                        : result.error()
        );
    }

    private static String messageOf(RuntimeException exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank()
                ? exception.getClass().getSimpleName()
                : message;
    }
}