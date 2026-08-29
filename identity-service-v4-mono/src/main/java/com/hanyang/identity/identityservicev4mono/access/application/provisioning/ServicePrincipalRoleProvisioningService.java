package com.hanyang.identity.identityservicev4mono.access.application.provisioning;


import com.hanyang.identity.identityservicev4mono.access.application.port.IdentityProviderServicePrincipalAccessPort;
import com.hanyang.identity.identityservicev4mono.access.domain.*;
import com.hanyang.identity.identityservicev4mono.service_identity.application.port.ServicePrincipalAccessSynchronizationPort;
import com.hanyang.identity.identityservicev4mono.service_identity.application.provisioning.ServicePrincipalProvisioningState;
import com.hanyang.identity.identityservicev4mono.service_identity.application.provisioning.ServicePrincipalProvisioningStateRepository;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipal;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalId;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalRepository;
import com.hanyang.identity.identityservicev4mono.shared.identityprovider.IdentityProviderType;
import com.hanyang.identity.identityservicev4mono.shared.outbox.OutboxPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ServicePrincipalRoleProvisioningService
        implements ServicePrincipalAccessSynchronizationPort {

    public static final String OUTBOX_AGGREGATE_TYPE = "SERVICE_PRINCIPAL_ROLE";
    public static final String OUTBOX_EVENT_TYPE =
            "SERVICE_PRINCIPAL_ROLE_PROVISIONING_REQUESTED";

    private static final IdentityProviderType PROVIDER =
            IdentityProviderType.KEYCLOAK;

    private final ServicePrincipalRepository servicePrincipalRepository;
    private final RoleRepository roleRepository;
    private final ApplicationRepository applicationRepository;
    private final ServicePrincipalRoleRepository servicePrincipalRoleRepository;
    private final ServicePrincipalRoleProvisioningStateRepository provisioningStateRepository;
    private final ServicePrincipalProvisioningStateRepository servicePrincipalProvisioningStateRepository;
    private final RoleProvisioningService roleProvisioningService;
    private final RoleProvisioningStateRepository roleProvisioningStateRepository;
    private final IdentityProviderServicePrincipalAccessPort identityProviderAccessPort;
    private final OutboxPublisher outboxPublisher;
    private final Clock clock;

    @Transactional
    public void requestSynchronization(
            ServicePrincipalId servicePrincipalId,
            RoleId roleId,
            boolean desiredAssigned
    ) {
        provisioningStateRepository.requestSynchronization(
                servicePrincipalId,
                roleId,
                PROVIDER,
                desiredAssigned
        );

        outboxPublisher.publish(
                OUTBOX_AGGREGATE_TYPE,
                new ServicePrincipalRoleProvisioningKey(
                        servicePrincipalId,
                        roleId
                ).serialize(),
                OUTBOX_EVENT_TYPE,
                null
        );
    }

    /**
     * Called after machine identity provisioning has established/updated the
     * external service-account client. Existing desired role assignments are
     * re-emitted through the same revisioned outbox path.
     */
    @Override
    @Transactional
    public void requestAssignedRolesSynchronization(
            ServicePrincipalId servicePrincipalId
    ) {
        for (RoleId roleId :
                servicePrincipalRoleRepository.findRoleIdsByServicePrincipalId(
                        servicePrincipalId
                )) {
            requestSynchronization(servicePrincipalId, roleId, true);
        }
    }

    public ServicePrincipalRoleReconciliationResult reconcile(
            ServicePrincipalId servicePrincipalId,
            RoleId roleId
    ) {
        ServicePrincipalRoleProvisioningState syncingState;

        try {
            if (provisioningStateRepository
                    .findByKeyAndProvider(servicePrincipalId, roleId, PROVIDER)
                    .isEmpty()) {
                provisioningStateRepository.requestSynchronization(
                        servicePrincipalId,
                        roleId,
                        PROVIDER,
                        servicePrincipalRoleRepository.exists(
                                servicePrincipalId,
                                roleId
                        )
                );
            }

            syncingState = provisioningStateRepository.beginSynchronization(
                    servicePrincipalId,
                    roleId,
                    PROVIDER
            );
        } catch (RuntimeException exception) {
            return ServicePrincipalRoleReconciliationResult.failed(
                    servicePrincipalId,
                    roleId,
                    PROVIDER,
                    false,
                    messageOf(exception)
            );
        }

        long synchronizedRevision = syncingState.getDesiredRevision();
        boolean desiredAssigned = syncingState.isDesiredAssigned();

        try {
            ServicePrincipal servicePrincipal = servicePrincipalRepository
                    .findById(servicePrincipalId)
                    .orElseThrow(() -> new IllegalStateException(
                            "Service principal not found: "
                                    + servicePrincipalId.value()
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
                ensureRoleCanBeAssigned(role, application);

                identityProviderAccessPort.assignRole(
                        requireExternalServicePrincipalId(servicePrincipal),
                        application.getCode(),
                        role.getCode()
                );
            } else {
                currentExternalServicePrincipalId(servicePrincipal.getId())
                        .ifPresent(externalServicePrincipalId ->
                                identityProviderAccessPort.revokeRole(
                                        externalServicePrincipalId,
                                        application.getCode(),
                                        role.getCode()
                                )
                        );
            }

            Instant synchronizedAt = clock.instant();
            ServicePrincipalRoleProvisioningState synchronizedState =
                    provisioningStateRepository.completeSynchronization(
                            servicePrincipalId,
                            roleId,
                            PROVIDER,
                            synchronizedRevision,
                            synchronizedAt
                    );

            return ServicePrincipalRoleReconciliationResult.fromState(
                    synchronizedState
            );
        } catch (RuntimeException exception) {
            ServicePrincipalRoleProvisioningState failedState =
                    provisioningStateRepository.failSynchronization(
                            servicePrincipalId,
                            roleId,
                            PROVIDER,
                            synchronizedRevision,
                            messageOf(exception)
                    );

            return ServicePrincipalRoleReconciliationResult.fromState(
                    failedState
            );
        }
    }

    private String requireExternalServicePrincipalId(
            ServicePrincipal servicePrincipal
    ) {
        return currentExternalServicePrincipalId(servicePrincipal.getId())
                .orElseThrow(() -> new IllegalStateException(
                        "Service principal has no Keycloak binding before role assignment: "
                                + servicePrincipal.getId().value()
                ));
    }

    private Optional<String> currentExternalServicePrincipalId(
            ServicePrincipalId servicePrincipalId
    ) {
        return servicePrincipalProvisioningStateRepository
                .findByServicePrincipalIdAndProvider(
                        servicePrincipalId,
                        PROVIDER
                )
                .map(ServicePrincipalProvisioningState::getExternalId)
                .filter(externalId -> externalId != null && !externalId.isBlank());
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

        RoleReconciliationResult result = roleProvisioningService.reconcile(
                role.getId()
        );
        if (result.status() == RoleProvisioningStatus.SYNCED) {
            return;
        }

        throw new IllegalStateException(
                result.error() == null || result.error().isBlank()
                        ? "Role provisioning did not complete before service-principal-role synchronization. "
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