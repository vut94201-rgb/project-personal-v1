package com.hanyang.identity.identityservicev4mono.access.application.provisioning;


import com.hanyang.identity.identityservicev4mono.access.application.port.IdentityProviderApplicationPort;
import com.hanyang.identity.identityservicev4mono.access.domain.Application;
import com.hanyang.identity.identityservicev4mono.access.domain.ApplicationId;
import com.hanyang.identity.identityservicev4mono.access.domain.ApplicationRepository;
import com.hanyang.identity.identityservicev4mono.access.domain.ApplicationStatus;
import com.hanyang.identity.identityservicev4mono.shared.identityprovider.IdentityProviderType;
import com.hanyang.identity.identityservicev4mono.shared.outbox.OutboxPublisher;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class ApplicationProvisioningService {

    static final String OUTBOX_AGGREGATE_TYPE = "APPLICATION";
    static final String OUTBOX_EVENT_TYPE = "APPLICATION_PROVISIONING_REQUESTED";

    private static final IdentityProviderType PROVIDER = IdentityProviderType.KEYCLOAK;

    private final ApplicationRepository applicationRepository;
    private final ApplicationProvisioningStateRepository provisioningStateRepository;
    private final IdentityProviderApplicationPort identityProviderApplicationPort;
    private final OutboxPublisher outboxPublisher;
    private final Clock clock;

    @Transactional
    public void requestSynchronization(ApplicationId applicationId) {
        provisioningStateRepository.requestSynchronization(applicationId, PROVIDER);
        outboxPublisher.publish(
                OUTBOX_AGGREGATE_TYPE,
                applicationId.value().toString(),
                OUTBOX_EVENT_TYPE,
                null
        );
    }

    public ApplicationReconciliationResult reconcile(ApplicationId applicationId) {
        if (applicationRepository.findById(applicationId).isEmpty()) {
            return ApplicationReconciliationResult.failed(
                    applicationId,
                    PROVIDER,
                    "Application not found: " + applicationId.value()
            );
        }

        ApplicationProvisioningState syncingState = provisioningStateRepository
                .beginSynchronization(applicationId, PROVIDER);
        long synchronizedRevision = syncingState.getDesiredRevision();

        Application application = applicationRepository
                .findById(applicationId)
                .orElseThrow(() -> new IllegalStateException(
                        "Application disappeared during reconciliation: " + applicationId.value()
                ));

        try {
            IdentityProviderApplicationPort.ProvisionedApplication provisionedApplication =
                    identityProviderApplicationPort.ensureApplication(
                            application.getCode(),
                            application.getName(),
                            application.getStatus() == ApplicationStatus.ACTIVE
                    );

            Instant synchronizedAt = clock.instant();
            ApplicationProvisioningState synchronizedState = provisioningStateRepository
                    .completeSynchronization(
                            applicationId,
                            PROVIDER,
                            synchronizedRevision,
                            provisionedApplication.externalId(),
                            provisionedApplication.externalCode(),
                            synchronizedAt
                    );

            return ApplicationReconciliationResult.fromState(synchronizedState);
        } catch (RuntimeException exception) {
            ApplicationProvisioningState failedState = provisioningStateRepository
                    .failSynchronization(
                            applicationId,
                            PROVIDER,
                            synchronizedRevision,
                            messageOf(exception)
                    );

            return ApplicationReconciliationResult.fromState(failedState);
        }
    }

    private static String messageOf(RuntimeException exception) {
        String message = exception.getMessage();
        return message == null || message.isBlank()
                ? exception.getClass().getSimpleName()
                : message;
    }
}