package com.hanyang.identity.identityservicev4mono.access.application.provisioning;


import com.hanyang.identity.identityservicev4mono.shared.outbox.OutboxEvent;
import com.hanyang.identity.identityservicev4mono.shared.outbox.OutboxEventHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AccountRoleProvisioningOutboxHandler implements OutboxEventHandler {

    private final AccountRoleProvisioningService provisioningService;

    @Override
    public boolean supports(String eventType) {
        return AccountRoleProvisioningService.OUTBOX_EVENT_TYPE.equals(eventType);
    }

    @Override
    public void handle(OutboxEvent event) {
        if (!AccountRoleProvisioningService.OUTBOX_AGGREGATE_TYPE.equals(event.aggregateType())) {
            throw new IllegalArgumentException(
                    "Unexpected aggregate type for account-role provisioning event: "
                            + event.aggregateType()
            );
        }

        AccountRoleProvisioningKey key = AccountRoleProvisioningKey.parse(
                event.aggregateId()
        );

        AccountRoleReconciliationResult result = provisioningService.reconcile(
                key.accountId(),
                key.roleId()
        );

        if (result.status() == AccountRoleProvisioningStatus.SYNCED
                || result.status() == AccountRoleProvisioningStatus.PENDING) {
            return;
        }

        throw new IllegalStateException(
                result.error() == null || result.error().isBlank()
                        ? "Account-role provisioning did not complete successfully. status="
                        + result.status()
                        + ", aggregateId="
                        + event.aggregateId()
                        : result.error()
        );
    }
}