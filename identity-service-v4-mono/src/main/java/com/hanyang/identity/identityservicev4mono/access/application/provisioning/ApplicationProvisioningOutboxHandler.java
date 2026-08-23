package com.hanyang.identity.identityservicev4mono.access.application.provisioning;

import com.hanyang.identity.identityservicev4mono.access.domain.ApplicationId;
import com.hanyang.identity.identityservicev4mono.shared.outbox.OutboxEvent;
import com.hanyang.identity.identityservicev4mono.shared.outbox.OutboxEventHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class ApplicationProvisioningOutboxHandler implements OutboxEventHandler {

            private final ApplicationProvisioningService provisioningService;

            @Override
    public boolean supports(String eventType) {
                return ApplicationProvisioningService.OUTBOX_EVENT_TYPE.equals(eventType);
            }

            @Override
    public void handle(OutboxEvent event) {
             if (!ApplicationProvisioningService.OUTBOX_AGGREGATE_TYPE.equals(event.aggregateType())) {
                        throw new IllegalArgumentException(
                    "Unexpected aggregate type for application provisioning event: "
                                                        + event.aggregateType()
                                        );
                    }
        UUID applicationUuid;
                try {
                        applicationUuid = UUID.fromString(event.aggregateId());
                    } catch (IllegalArgumentException exception) {
                        throw new IllegalArgumentException(
                                        "Invalid application id in outbox event: " + event.aggregateId(),
                    exception
                                        );
                    }

                        ApplicationReconciliationResult result = provisioningService.reconcile(
                                new ApplicationId(applicationUuid)
                              );

                        if (result.status() == ApplicationProvisioningStatus.SYNCED
                                || result.status() == ApplicationProvisioningStatus.PENDING) {
                        return;
                   }

                        throw new IllegalStateException(
                         result.error() == null || result.error().isBlank()
                                                ? "Application provisioning did not complete successfully. status="
                                                        + result.status()
                                                        + ", applicationId="
                                                        + event.aggregateId()
                                                : result.error()
                                );
            }
}