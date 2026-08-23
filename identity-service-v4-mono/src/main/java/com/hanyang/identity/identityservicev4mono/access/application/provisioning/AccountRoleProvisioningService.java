package com.hanyang.identity.identityservicev4mono.access.application.provisioning;


import com.hanyang.identity.identityservicev4mono.access.application.port.IdentityProviderAccessPort;
import com.hanyang.identity.identityservicev4mono.access.domain.*;
import com.hanyang.identity.identityservicev4mono.account.application.provisioning.AccountProvisioningService;
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
    private final AccountProvisioningService accountProvisioningService;
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
                account = ensureAccountCanReceiveRole(account);
                ensureRoleCanBeAssigned(role, application);

                identityProviderAccessPort.assignRole(
                        account.getKeycloakSubject(),
                        application.getCode(),
                        role.getCode()
                );
            } else if (hasExternalSubject(account)) {
                identityProviderAccessPort.revokeRole(
                        account.getKeycloakSubject(),
                        application.getCode(),
                        role.getCode()
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

    private Account ensureAccountCanReceiveRole(Account account) {
        if (account.getStatus() == AccountStatus.DISABLED) {
            throw new IllegalStateException(
                    "Cannot assign identity-provider role to disabled account: "
                            + account.getId().value()
            );
        }

        if (account.getStatus() != AccountStatus.ACTIVE || !hasExternalSubject(account)) {
            AccountId accountId = account.getId();
            accountProvisioningService.reconcile(accountId);
            account = accountRepository.findById(accountId)
                    .orElseThrow(() -> new IllegalStateException(
                            "Account disappeared during provisioning: "
                                    + accountId.value()
                    ));
        }

        if (account.getStatus() != AccountStatus.ACTIVE || !hasExternalSubject(account)) {
            throw new IllegalStateException(
                    "Account provisioning did not complete before role assignment: "
                            + account.getId().value()
            );
        }

        return account;
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

    private static boolean hasExternalSubject(Account account) {
        return account.getKeycloakSubject() != null
                && !account.getKeycloakSubject().isBlank();
    }

    private static String messageOf(RuntimeException exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank()
                ? exception.getClass().getSimpleName()
                : message;
    }
}