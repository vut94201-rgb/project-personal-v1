package com.hanyang.identity.identityservicev4mono.access.api.rest.controller;

import com.hanyang.identity.identityservicev4mono.access.api.mapper.ApplicationRestMapper;
import com.hanyang.identity.identityservicev4mono.access.api.rest.request.CreateApplicationRequest;
import com.hanyang.identity.identityservicev4mono.access.api.rest.request.UpdateApplicationRequest;
import com.hanyang.identity.identityservicev4mono.access.api.rest.response.ApplicationResponse;
import com.hanyang.identity.identityservicev4mono.access.application.ApplicationCommandService;
import com.hanyang.identity.identityservicev4mono.access.application.ApplicationQueryService;
import com.hanyang.identity.identityservicev4mono.access.application.command.UpdateApplicationCommand;
import com.hanyang.identity.identityservicev4mono.access.domain.Application;
import com.hanyang.identity.identityservicev4mono.access.domain.ApplicationId;
import com.hanyang.identity.identityservicev4mono.access.domain.ApplicationStatus;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/applications")
@RequiredArgsConstructor
public class ApplicationController {

  private final ApplicationCommandService commandService;
  private final ApplicationQueryService queryService;
  private final ApplicationRestMapper mapper;

  @PostMapping
  public ResponseEntity<ApplicationResponse> create(
          @Valid @RequestBody CreateApplicationRequest request) {
    Application application = commandService.create(mapper.toCommand(request));

    return ResponseEntity.status(HttpStatus.CREATED).body(mapper.toResponse(application));
  }

  @GetMapping("/{id}")
  public ApplicationResponse getById(@PathVariable UUID id) {
    return mapper.toResponse(queryService.getById(new ApplicationId(id)));
  }

  @GetMapping("/by-code/{code}")
  public ApplicationResponse getByCode(@PathVariable String code) {
    return mapper.toResponse(queryService.getByCode(code));
  }

  @PutMapping("/{id}")
  public ApplicationResponse update(
          @PathVariable UUID id, @Valid @RequestBody UpdateApplicationRequest request) {
    Application application =
            commandService.update(new UpdateApplicationCommand(new ApplicationId(id), request.name()));

    return mapper.toResponse(application);
  }

  @PatchMapping("/{id}/disable")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void disable(@PathVariable UUID id) {
    commandService.disable(new ApplicationId(id));
  }

  @GetMapping("/get-active")
  public Set<ApplicationResponse> getAllActiveApplications() {
    return queryService.getAllApplicationByStatus(ApplicationStatus.ACTIVE).stream()
            .map(mapper::toResponse)
            .collect(Collectors.toSet());
  }
}