package com.hanyang.identity.identityservicev4mono.access.api.rest.controller;

import com.hanyang.identity.identityservicev4mono.access.api.mapper.RoleRestMapper;
import com.hanyang.identity.identityservicev4mono.access.api.rest.response.RoleResponse;
import com.hanyang.identity.identityservicev4mono.access.application.AccountRoleCommandService;
import com.hanyang.identity.identityservicev4mono.access.application.AccountRoleQueryService;
import com.hanyang.identity.identityservicev4mono.access.domain.RoleId;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountId;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/accounts/{accountId}/roles")
@RequiredArgsConstructor
@Slf4j
public class AccountRoleController {

  private final AccountRoleCommandService commandService;
  private final AccountRoleQueryService queryService;
  private final RoleRestMapper roleMapper;

  @GetMapping
  public List<RoleResponse> getRoles(@PathVariable UUID accountId) {
    return queryService.getRoles(new AccountId(accountId)).stream()
        .map(roleMapper::toResponse)
        .toList();
  }

  @PostMapping("/assign-role")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void assign(@PathVariable UUID accountId, @RequestParam UUID roleId) {

    commandService.assign(new AccountId(accountId), new RoleId(roleId));
  }

  @DeleteMapping("/{roleId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void revoke(@PathVariable UUID accountId, @PathVariable UUID roleId) {
    commandService.revoke(new AccountId(accountId), new RoleId(roleId));
  }
}
