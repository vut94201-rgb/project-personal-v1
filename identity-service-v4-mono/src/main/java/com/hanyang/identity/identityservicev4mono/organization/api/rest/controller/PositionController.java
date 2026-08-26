package com.hanyang.identity.identityservicev4mono.organization.api.rest.controller;

import com.hanyang.identity.identityservicev4mono.organization.api.mapper.PositionRestMapper;
import com.hanyang.identity.identityservicev4mono.organization.api.rest.request.CreatePositionRequest;
import com.hanyang.identity.identityservicev4mono.organization.api.rest.request.UpdatePositionRequest;
import com.hanyang.identity.identityservicev4mono.organization.api.rest.response.PositionResponse;
import com.hanyang.identity.identityservicev4mono.organization.application.PositionCommandService;
import com.hanyang.identity.identityservicev4mono.organization.application.PositionQueryService;
import com.hanyang.identity.identityservicev4mono.organization.application.command.UpdatePositionCommand;
import com.hanyang.identity.identityservicev4mono.organization.domain.OrganizationReferenceStatus;
import com.hanyang.identity.identityservicev4mono.organization.domain.PositionId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/positions")
@RequiredArgsConstructor
public class PositionController {
    private final PositionCommandService commandService;
    private final PositionQueryService queryService;
    private final PositionRestMapper mapper;

    @PostMapping
    public ResponseEntity<PositionResponse> create(@Valid @RequestBody CreatePositionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(mapper.toResponse(commandService.create(mapper.toCommand(request))));
    }

    @GetMapping("/{id}")
    public PositionResponse getById(@PathVariable UUID id) {
        return mapper.toResponse(queryService.getById(new PositionId(id)));
    }

    @GetMapping("/by-code/{code}")
    public PositionResponse getByCode(@PathVariable String code) {
        return mapper.toResponse(queryService.getByCode(code));
    }

    @GetMapping
    public List<PositionResponse> findAll(@RequestParam(required = false) String status) {
        OrganizationReferenceStatus parsed = OrganizationReferenceStatus.fromExternalValue(status);
        return queryService.findAll(parsed).stream().map(mapper::toResponse).toList();
    }

    @PutMapping("/{id}")
    public PositionResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePositionRequest request
    ) {
        return mapper.toResponse(commandService.update(
                new UpdatePositionCommand(new PositionId(id), request.name())
        ));
    }

    @PatchMapping("/{id}/disable")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disable(@PathVariable UUID id) {
        commandService.disable(new PositionId(id));
    }
}
