package com.hanyang.identity.identityservicev4mono.service_identity.application.provisioning;


import com.hanyang.identity.identityservicev4mono.service_identity.application.exception.ServicePrincipalNotFoundException;
import com.hanyang.identity.identityservicev4mono.service_identity.application.lifecycle.ServicePrincipalActivationCoordinator;
import com.hanyang.identity.identityservicev4mono.service_identity.application.port.IdentityProviderServicePrincipalPort;
import com.hanyang.identity.identityservicev4mono.service_identity.application.port.ServicePrincipalAccessSynchronizationPort;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipal;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalId;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalRepository;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalStatus;
import com.hanyang.identity.identityservicev4mono.shared.identityprovider.IdentityProviderType;
import com.hanyang.identity.identityservicev4mono.shared.outbox.OutboxPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ServicePrincipalProvisioningService {

    public static final String OUTBOX_AGGREGATE_TYPE = "SERVICE_PRINCIPAL";
    public static final String OUTBOX_EVENT_TYPE =
            "SERVICE_PRINCIPAL_PROVISIONING_REQUESTED";

    private static final IdentityProviderType PROVIDER =
            IdentityProviderType.KEYCLOAK;

    private final ServicePrincipalRepository servicePrincipalRepository;
    private final ServicePrincipalProvisioningStateRepository provisioningStateRepository;
    private final IdentityProviderServicePrincipalPort identityProviderServicePrincipalPort;
    private final ServicePrincipalActivationCoordinator activationCoordinator;
    private final ServicePrincipalAccessSynchronizationPort accessSynchronizationPort;
    private final OutboxPublisher outboxPublisher;
    private final Clock clock;

    @Transactional
    public void requestSynchronization(
            ServicePrincipalId servicePrincipalId
    ) {
        if (servicePrincipalRepository.findById(servicePrincipalId).isEmpty()) {
            throw new ServicePrincipalNotFoundException(servicePrincipalId);
        }

        provisioningStateRepository.requestSynchronization(
                servicePrincipalId,
                PROVIDER
        );

        outboxPublisher.publish(
                OUTBOX_AGGREGATE_TYPE,
                servicePrincipalId.value().toString(),
                OUTBOX_EVENT_TYPE,
                null
        );
    }

    /**
     * Reconciles one durable desired revision with the external provider.
     *
     * <p>No remote call is made inside the business command transaction that
     * requested synchronization. This method is invoked by outbox processing
     * (and can later be reused by admin retry/drift repair).</p>
     */
    public ServicePrincipalReconciliationResult reconcile(
            ServicePrincipalId servicePrincipalId
    ) {
        ServicePrincipal servicePrincipal = servicePrincipalRepository
                .findById(servicePrincipalId)
                .orElse(null);

        if (servicePrincipal == null) {
            return ServicePrincipalReconciliationResult.failed(
                    servicePrincipalId,
                    PROVIDER,
                    "Service principal not found: " + servicePrincipalId.value()
            );
        }

        ServicePrincipalProvisioningState syncingState =
                provisioningStateRepository.beginSynchronization(
                        servicePrincipalId,
                        PROVIDER
                );
        long synchronizedRevision = syncingState.getDesiredRevision();

        ServicePrincipalProvisioningState synchronizedState;
        try {
            boolean attemptedEnabled = authenticationAllowed(
                    servicePrincipal.getStatus()
            );

            IdentityProviderServicePrincipalPort.ProvisionedServicePrincipal
                    provisionedServicePrincipal =
                    synchronizeExternalPrincipal(
                            servicePrincipal,
                            syncingState.getExternalId(),
                            attemptedEnabled
                    );

            ServicePrincipal latest = servicePrincipalRepository
                    .findById(servicePrincipalId)
                    .orElseThrow(() -> new IllegalStateException(
                            "Service principal disappeared during reconciliation: "
                                    + servicePrincipalId.value()
                    ));

            boolean latestEnabled = authenticationAllowed(latest.getStatus());
            if (attemptedEnabled != latestEnabled) {
                provisionedServicePrincipal = synchronizeExternalPrincipal(
                        latest,
                        provisionedServicePrincipal.externalId(),
                        latestEnabled
                );
            }

            Instant synchronizedAt = clock.instant();
            synchronizedState = provisioningStateRepository
                    .completeSynchronization(
                            servicePrincipalId,
                            PROVIDER,
                            synchronizedRevision,
                            provisionedServicePrincipal.externalId(),
                            provisionedServicePrincipal.externalCode(),
                            synchronizedAt
                    );
        } catch (RuntimeException exception) {
            ServicePrincipalProvisioningState failedState =
                    provisioningStateRepository.failSynchronization(
                            servicePrincipalId,
                            PROVIDER,
                            synchronizedRevision,
                            messageOf(exception)
                    );

            return ServicePrincipalReconciliationResult.fromState(failedState);
        }

        // Provider state is durable before lifecycle coordination. If
        // activation publishes a new desired revision, it becomes a separate
        // outbox item and never turns the remote call into a distributed txn.
        activationCoordinator.afterIdentityProviderSynchronization(
                servicePrincipalId
        );

        // Role assignments are desired business state owned by Access. Once a
        // Keycloak client/service-account binding exists, re-emit those
        // assignments through their own revisioned outbox flow. No provider
        // role call happens in this transaction.
        ServicePrincipal latestAfterCoordination = servicePrincipalRepository
                .findById(servicePrincipalId)
                .orElse(null);
        if (latestAfterCoordination != null
                && latestAfterCoordination.getStatus()
                == ServicePrincipalStatus.ACTIVE) {
            accessSynchronizationPort.requestAssignedRolesSynchronization(
                    servicePrincipalId
            );
        }

        return ServicePrincipalReconciliationResult.fromState(
                synchronizedState
        );
    }

    private IdentityProviderServicePrincipalPort.ProvisionedServicePrincipal
    synchronizeExternalPrincipal(
            ServicePrincipal servicePrincipal,
            String externalId,
            boolean enabled
    ) {
        return identityProviderServicePrincipalPort
                .synchronizeServicePrincipal(
                        servicePrincipal.getCode(),
                        servicePrincipal.getDisplayName(),
                        servicePrincipal.getPurpose(),
                        externalId,
                        enabled
                );
    }

    private static boolean authenticationAllowed(
            ServicePrincipalStatus status
    ) {
        return status == ServicePrincipalStatus.ACTIVE;
    }

    private static String messageOf(RuntimeException exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank()
                ? exception.getClass().getSimpleName()
                : message;
    }
}