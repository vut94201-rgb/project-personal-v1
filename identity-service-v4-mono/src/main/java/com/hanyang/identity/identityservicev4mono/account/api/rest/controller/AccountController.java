package com.hanyang.identity.identityservicev4mono.account.api.rest.controller;

import com.hanyang.identity.identityservicev4mono.account.api.mapper.AccountRestMapper;
import com.hanyang.identity.identityservicev4mono.account.api.rest.request.CreateAccountRequest;
import com.hanyang.identity.identityservicev4mono.account.api.rest.response.AccountResponse;
import com.hanyang.identity.identityservicev4mono.account.application.AccountCommandService;
import com.hanyang.identity.identityservicev4mono.account.application.AccountQueryService;
import com.hanyang.identity.identityservicev4mono.account.domain.Account;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts")
@RequiredArgsConstructor
public class AccountController {

    private final AccountCommandService commandService;
    private final AccountQueryService queryService;
    private final AccountRestMapper mapper;

    @PostMapping
    public ResponseEntity<AccountResponse> create(
            @Valid @RequestBody CreateAccountRequest request
    ) {
        Account account =
                commandService.create(
                        mapper.toCommand(request)
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(mapper.toResponse(account));
    }

    @GetMapping("/{id}")
    public AccountResponse get(
            @PathVariable UUID id
    ) {
        return mapper.toResponse(
                queryService.getById(
                        new AccountId(id)
                )
        );
    }

    @PostMapping("/{id}/provision")
    public AccountResponse provision(
            @PathVariable UUID id
    ) {
        return mapper.toResponse(
                commandService.provision(
                        new AccountId(id)
                )
        );
    }

    @PatchMapping("/{id}/disable")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disable(
            @PathVariable UUID id
    ) {
        commandService.disable(
                new AccountId(id)
        );
    }
}