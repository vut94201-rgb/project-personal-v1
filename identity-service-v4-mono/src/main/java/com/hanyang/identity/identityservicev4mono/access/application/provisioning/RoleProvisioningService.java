package com.hanyang.identity.identityservicev4mono.access.application.provisioning;


import com.hanyang.identity.identityservicev4mono.access.application.port.IdentityProviderRolePort;
import com.hanyang.identity.identityservicev4mono.access.domain.*;
import com.hanyang.identity.identityservicev4mono.shared.identityprovider.IdentityProviderType;
import com.hanyang.identity.identityservicev4mono.shared.outbox.OutboxPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class RoleProvisioningService {

    static final String OUTBOX_AGGREGATE_TYPE = "ROLE";
    static final String OUTBOX_EVENT_TYPE = "ROLE_PROVISIONING_REQUESTED";

    private static final IdentityProviderType PROVIDER = IdentityProviderType.KEYCLOAK;

    private final RoleRepository roleRepository;
    private final ApplicationRepository applicationRepository;
    private final RoleProvisioningStateRepository provisioningStateRepository;
    private final ApplicationProvisioningStateRepository applicationProvisioningStateRepository;
    private final ApplicationProvisioningService applicationProvisioningService;
    private final IdentityProviderRolePort identityProviderRolePort;
    private final OutboxPublisher outboxPublisher;
    private final Clock clock;

    @Transactional
    public void requestSynchronization(RoleId roleId) {
        provisioningStateRepository.requestSynchronization(roleId, PROVIDER);
        outboxPublisher.publish(
                OUTBOX_AGGREGATE_TYPE,
                roleId.value().toString(),
                OUTBOX_EVENT_TYPE,
                null
        );
    }

    public RoleReconciliationResult reconcile(RoleId roleId) {
        Role role = roleRepository.findById(roleId).orElse(null);
        if (role == null) {
            return RoleReconciliationResult.failed(
                    roleId,
                    PROVIDER,
                    "Role not found: " + roleId.value()
            );
        }

        Application application = applicationRepository
                .findById(role.getApplicationId())
                .orElse(null);
        if (application == null) {
            return RoleReconciliationResult.failed(
                    roleId,
                    PROVIDER,
                    "Application not found for role "
                            + roleId.value()
                            + ": "
                            + role.getApplicationId().value()
            );
        }

        RoleProvisioningState syncingState = provisioningStateRepository
                .beginSynchronization(roleId, PROVIDER);
        long synchronizedRevision = syncingState.getDesiredRevision();

        try {
            ensureApplicationIsProvisioned(application);

            IdentityProviderRolePort.ProvisionedRole provisionedRole =
                    identityProviderRolePort.synchronizeRole(
                            application.getCode(),
                            role.getCode(),
                            role.getName(),
                            role.getStatus() == RoleStatus.ACTIVE
                    );

            Instant synchronizedAt = clock.instant();
            RoleProvisioningState synchronizedState = provisioningStateRepository
                    .completeSynchronization(
                            roleId,
                            PROVIDER,
                            synchronizedRevision,
                            provisionedRole.externalId(),
                            provisionedRole.externalCode(),
                            synchronizedAt
                    );

            return RoleReconciliationResult.fromState(synchronizedState);
        } catch (RuntimeException exception) {
            RoleProvisioningState failedState = provisioningStateRepository
                    .failSynchronization(
                            roleId,
                            PROVIDER,
                            synchronizedRevision,
                            messageOf(exception)
                    );

            return RoleReconciliationResult.fromState(failedState);
        }
    }

    private void ensureApplicationIsProvisioned(Application application) {
        boolean synchronizedApplication = applicationProvisioningStateRepository
                .findByApplicationIdAndProvider(application.getId(), PROVIDER)
                .filter(state -> state.getStatus() == ApplicationProvisioningStatus.SYNCED)
                .filter(state -> state.getExternalId() != null && !state.getExternalId().isBlank())
                .isPresent();

        if (synchronizedApplication) {
            return;
        }

        ApplicationReconciliationResult result = applicationProvisioningService
                .reconcile(application.getId());

        if ((result.status() == ApplicationProvisioningStatus.SYNCED
                || result.status() == ApplicationProvisioningStatus.PENDING)
                && result.externalId() != null
                && !result.externalId().isBlank()) {
            return;
        }

        throw new IllegalStateException(
                result.error() == null || result.error().isBlank()
                        ? "Application provisioning did not complete before role provisioning. "
                        + "applicationId="
                        + application.getId().value()
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