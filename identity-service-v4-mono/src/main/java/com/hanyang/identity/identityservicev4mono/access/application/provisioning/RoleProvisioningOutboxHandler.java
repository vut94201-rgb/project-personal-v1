package com.hanyang.identity.identityservicev4mono.access.application.provisioning;

import com.hanyang.identity.identityservicev4mono.access.domain.RoleId;
import com.hanyang.identity.identityservicev4mono.shared.outbox.OutboxEvent;
import com.hanyang.identity.identityservicev4mono.shared.outbox.OutboxEventHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RoleProvisioningOutboxHandler implements OutboxEventHandler {

    private final RoleProvisioningService provisioningService;

    @Override
    public boolean supports(String eventType) {
        return RoleProvisioningService.OUTBOX_EVENT_TYPE.equals(eventType);
    }

    @Override
    public void handle(OutboxEvent event) {
        if (!RoleProvisioningService.OUTBOX_AGGREGATE_TYPE.equals(event.aggregateType())) {
            throw new IllegalArgumentException(
                    "Unexpected aggregate type for role provisioning event: "
                            + event.aggregateType()
            );
        }

        UUID roleUuid;
        try {
            roleUuid = UUID.fromString(event.aggregateId());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid role id in outbox event: " + event.aggregateId(),
                    exception
            );
        }

        RoleReconciliationResult result = provisioningService.reconcile(
                new RoleId(roleUuid)
        );

        if (result.status() == RoleProvisioningStatus.SYNCED
                || result.status() == RoleProvisioningStatus.PENDING) {
            return;
        }

        throw new IllegalStateException(
                result.error() == null || result.error().isBlank()
                        ? "Role provisioning did not complete successfully. status="
                        + result.status()
                        + ", roleId="
                        + event.aggregateId()
                        : result.error()
        );
    }
}