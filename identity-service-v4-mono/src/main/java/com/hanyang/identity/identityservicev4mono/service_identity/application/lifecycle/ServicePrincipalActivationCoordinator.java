package com.hanyang.identity.identityservicev4mono.service_identity.application.lifecycle;


import com.hanyang.identity.identityservicev4mono.service_identity.application.provisioning.ServicePrincipalProvisioningService;
import com.hanyang.identity.identityservicev4mono.service_identity.application.provisioning.ServicePrincipalProvisioningState;
import com.hanyang.identity.identityservicev4mono.service_identity.application.provisioning.ServicePrincipalProvisioningStateRepository;
import com.hanyang.identity.identityservicev4mono.service_identity.application.provisioning.ServicePrincipalProvisioningStatus;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipal;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalId;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalRepository;
import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalStatus;
import com.hanyang.identity.identityservicev4mono.shared.identityprovider.IdentityProviderType;
import com.hanyang.identity.identityservicev4mono.shared.outbox.OutboxPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Owns PENDING -> ACTIVE for machine identities.
 *
 * <p>A service principal is first synchronized to Keycloak while disabled.
 * Only after the required provider binding is durably current does local
 * business state become ACTIVE. A fresh desired revision is then emitted so
 * the external client converges to enabled.</p>
 */
@Service
@RequiredArgsConstructor
public class ServicePrincipalActivationCoordinator {

    private static final IdentityProviderType PROVIDER =
            IdentityProviderType.KEYCLOAK;

    private final ServicePrincipalRepository servicePrincipalRepository;
    private final ServicePrincipalProvisioningStateRepository provisioningStateRepository;
    private final OutboxPublisher outboxPublisher;

    @Transactional
    public void afterIdentityProviderSynchronization(
            ServicePrincipalId servicePrincipalId
    ) {
        ServicePrincipal servicePrincipal = servicePrincipalRepository
                .findById(servicePrincipalId)
                .orElse(null);

        if (servicePrincipal == null
                || servicePrincipal.getStatus() != ServicePrincipalStatus.PENDING) {
            return;
        }

        ServicePrincipalProvisioningState state = provisioningStateRepository
                .findByServicePrincipalIdAndProvider(servicePrincipalId, PROVIDER)
                .orElse(null);

        if (!isCurrent(state)) {
            return;
        }

        if (!hasExternalBinding(state)) {
            throw new IllegalStateException(
                    "Service principal provisioning is SYNCED but has no external binding. "
                            + "servicePrincipalId="
                            + servicePrincipalId.value()
            );
        }

        servicePrincipal.activate();
        servicePrincipalRepository.save(servicePrincipal);

        // The first provider reconciliation deliberately created/updated a
        // disabled confidential client. ACTIVE is now the business truth, so
        // publish a fresh revision that enables client-credentials access.
        provisioningStateRepository.requestSynchronization(
                servicePrincipalId,
                PROVIDER
        );
        outboxPublisher.publish(
                ServicePrincipalProvisioningService.OUTBOX_AGGREGATE_TYPE,
                servicePrincipalId.value().toString(),
                ServicePrincipalProvisioningService.OUTBOX_EVENT_TYPE,
                null
        );
    }

    private static boolean isCurrent(
            ServicePrincipalProvisioningState state
    ) {
        return state != null
                && state.getStatus() == ServicePrincipalProvisioningStatus.SYNCED
                && state.getSyncedRevision() == state.getDesiredRevision()
                && state.getLastSyncedAt() != null;
    }

    private static boolean hasExternalBinding(
            ServicePrincipalProvisioningState state
    ) {
        return state.getExternalId() != null
                && !state.getExternalId().isBlank();
    }
}