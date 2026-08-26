package com.hanyang.identity.identityservicev4mono.shared.outbox.api.rest.controller;


import com.hanyang.identity.identityservicev4mono.security.authorization.IdentityAdminAccess;
import com.hanyang.identity.identityservicev4mono.shared.outbox.OutboxOperationsService;
import com.hanyang.identity.identityservicev4mono.shared.outbox.api.rest.respone.OutboxEventOperationsResponse;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/internal/v1/operations/outbox")
@RequiredArgsConstructor
@IdentityAdminAccess
@Validated
public class OutboxOperationsController {

    private final OutboxOperationsService operationsService;

    @GetMapping("/dead")
    public List<OutboxEventOperationsResponse> findDeadEvents(
            @RequestParam(defaultValue = "100")
            @Min(1)
            @Max(500)
            int limit
    ) {
        return operationsService.findDeadEvents(limit).stream()
                .map(OutboxEventOperationsResponse::from)
                .toList();
    }

    @PostMapping("/{eventId}/retry")
    public OutboxEventOperationsResponse retryDeadEvent(
            @PathVariable UUID eventId
    ) {
        return OutboxEventOperationsResponse.from(
                operationsService.retryDeadEvent(eventId)
        );
    }
}