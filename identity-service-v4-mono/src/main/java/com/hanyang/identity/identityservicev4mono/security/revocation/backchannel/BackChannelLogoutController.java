package com.hanyang.identity.identityservicev4mono.security.revocation.backchannel;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/oidc")
public class BackChannelLogoutController {  private final BackChannelLogoutService logoutService;

    public BackChannelLogoutController(
            BackChannelLogoutService logoutService
    ) {
        this.logoutService = logoutService;
    }

    @PostMapping(
            value = "/backchannel-logout",
            consumes = MediaType.APPLICATION_FORM_URLENCODED_VALUE
    )
    public ResponseEntity<Void> backchannelLogout(
            @RequestParam("logout_token") String logoutToken
    ) {
        logoutService.handle(logoutToken);

        return ResponseEntity.noContent().build();
    }}
