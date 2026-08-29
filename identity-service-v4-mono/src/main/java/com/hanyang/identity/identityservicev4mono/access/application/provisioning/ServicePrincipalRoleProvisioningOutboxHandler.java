package com.hanyang.identity.identityservicev4mono.access.application.provisioning;


import com.hanyang.identity.identityservicev4mono.shared.outbox.OutboxEvent;
import com.hanyang.identity.identityservicev4mono.shared.outbox.OutboxEventHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ServicePrincipalRoleProvisioningOutboxHandler
        implements OutboxEventHandler {

    private final ServicePrincipalRoleProvisioningService provisioningService;

    @Override
    public boolean supports(String eventType) {
        return ServicePrincipalRoleProvisioningService.OUTBOX_EVENT_TYPE
                .equals(eventType);
    }

    @Override
    public void handle(OutboxEvent event) {
        if (!ServicePrincipalRoleProvisioningService.OUTBOX_AGGREGATE_TYPE
                .equals(event.aggregateType())) {
            throw new IllegalArgumentException(
                    "Unexpected aggregate type for service-principal-role provisioning event: "
                            + event.aggregateType()
            );
        }

        ServicePrincipalRoleProvisioningKey key =
                ServicePrincipalRoleProvisioningKey.parse(event.aggregateId());

        ServicePrincipalRoleReconciliationResult result =
                provisioningService.reconcile(
                        key.servicePrincipalId(),
                        key.roleId()
                );

        if (result.status() == ServicePrincipalRoleProvisioningStatus.SYNCED
                || result.status() == ServicePrincipalRoleProvisioningStatus.PENDING) {
            return;
        }

        throw new IllegalStateException(
                result.error() == null || result.error().isBlank()
                        ? "Service-principal-role provisioning did not complete successfully. status="
                        + result.status()
                        + ", aggregateId="
                        + event.aggregateId()
                        : result.error()
        );
    }
}