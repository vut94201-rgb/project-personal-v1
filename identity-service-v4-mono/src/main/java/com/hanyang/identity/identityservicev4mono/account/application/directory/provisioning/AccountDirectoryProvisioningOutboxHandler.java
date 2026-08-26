package com.hanyang.identity.identityservicev4mono.account.application.directory.provisioning;


import com.hanyang.identity.identityservicev4mono.account.domain.AccountId;
import com.hanyang.identity.identityservicev4mono.shared.outbox.OutboxEvent;
import com.hanyang.identity.identityservicev4mono.shared.outbox.OutboxEventHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@ConditionalOnProperty(
        prefix = "integration.ds389",
        name = "enabled",
        havingValue = "true"
)
public class AccountDirectoryProvisioningOutboxHandler
        implements OutboxEventHandler {

    private final AccountDirectoryProvisioningService provisioningService;

    @Override
    public boolean supports(String eventType) {
        return AccountDirectoryProvisioningService.OUTBOX_EVENT_TYPE.equals(eventType);
    }

    @Override
    public void handle(OutboxEvent event) {
        if (!AccountDirectoryProvisioningService.OUTBOX_AGGREGATE_TYPE
                .equals(event.aggregateType())) {
            throw new IllegalArgumentException(
                    "Unexpected aggregate type for account directory provisioning event: "
                            + event.aggregateType()
            );
        }

        UUID accountUuid;
        try {
            accountUuid = UUID.fromString(event.aggregateId());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid account id in directory provisioning event: "
                            + event.aggregateId(),
                    exception
            );
        }

        AccountDirectoryReconciliationResult result =
                provisioningService.reconcile(new AccountId(accountUuid));

        if (result.status() == AccountDirectoryProvisioningStatus.SYNCED
                || result.status() == AccountDirectoryProvisioningStatus.PENDING) {
            return;
        }

        throw new IllegalStateException(
                result.error() == null || result.error().isBlank()
                        ? "Account directory provisioning did not complete successfully. status="
                        + result.status()
                        + ", accountId="
                        + event.aggregateId()
                        : result.error()
        );
    }
}