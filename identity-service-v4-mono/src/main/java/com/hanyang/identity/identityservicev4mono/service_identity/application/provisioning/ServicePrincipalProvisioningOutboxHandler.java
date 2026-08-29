package com.hanyang.identity.identityservicev4mono.service_identity.application.provisioning;


import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalId;
import com.hanyang.identity.identityservicev4mono.shared.outbox.OutboxEvent;
import com.hanyang.identity.identityservicev4mono.shared.outbox.OutboxEventHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ServicePrincipalProvisioningOutboxHandler
        implements OutboxEventHandler {

    private final ServicePrincipalProvisioningService provisioningService;

    @Override
    public boolean supports(String eventType) {
        return ServicePrincipalProvisioningService.OUTBOX_EVENT_TYPE
                .equals(eventType);
    }

    @Override
    public void handle(OutboxEvent event) {
        if (!ServicePrincipalProvisioningService.OUTBOX_AGGREGATE_TYPE
                .equals(event.aggregateType())) {
            throw new IllegalArgumentException(
                    "Unexpected aggregate type for service principal "
                            + "provisioning event: "
                            + event.aggregateType()
            );
        }

        UUID servicePrincipalUuid;
        try {
            servicePrincipalUuid = UUID.fromString(event.aggregateId());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid service principal id in outbox event: "
                            + event.aggregateId(),
                    exception
            );
        }

        ServicePrincipalReconciliationResult result =
                provisioningService.reconcile(
                        new ServicePrincipalId(servicePrincipalUuid)
                );

        if (result.status() == ServicePrincipalProvisioningStatus.SYNCED
                || result.status() == ServicePrincipalProvisioningStatus.PENDING) {
            return;
        }

        throw new IllegalStateException(
                result.error() == null || result.error().isBlank()
                        ? "Service principal provisioning did not complete "
                        + "successfully. status="
                        + result.status()
                        + ", servicePrincipalId="
                        + event.aggregateId()
                        : result.error()
        );
    }
}