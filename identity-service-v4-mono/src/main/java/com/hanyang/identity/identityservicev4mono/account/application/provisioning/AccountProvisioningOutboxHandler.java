package com.hanyang.identity.identityservicev4mono.account.application.provisioning;


import com.hanyang.identity.identityservicev4mono.account.domain.AccountId;
import com.hanyang.identity.identityservicev4mono.shared.outbox.OutboxEvent;
import com.hanyang.identity.identityservicev4mono.shared.outbox.OutboxEventHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AccountProvisioningOutboxHandler implements OutboxEventHandler {

    private final AccountProvisioningService provisioningService;

    @Override
    public boolean supports(String eventType) {
        return AccountProvisioningService.OUTBOX_EVENT_TYPE.equals(eventType);
    }

    @Override
    public void handle(OutboxEvent event) {
        if (!AccountProvisioningService.OUTBOX_AGGREGATE_TYPE.equals(event.aggregateType())) {
            throw new IllegalArgumentException(
                    "Unexpected aggregate type for account provisioning event: "
                            + event.aggregateType()
            );
        }

        UUID accountUuid;
        try {
            accountUuid = UUID.fromString(event.aggregateId());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid account id in outbox event: " + event.aggregateId(),
                    exception
            );
        }

        AccountReconciliationResult result = provisioningService.reconcile(
                new AccountId(accountUuid)
        );

        if (result.status() == AccountProvisioningStatus.SYNCED
                || result.status() == AccountProvisioningStatus.PENDING) {
            return;
        }

        throw new IllegalStateException(
                result.error() == null || result.error().isBlank()
                        ? "Account provisioning did not complete successfully. status="
                        + result.status()
                        + ", accountId="
                        + event.aggregateId()
                        : result.error()
        );
    }
}