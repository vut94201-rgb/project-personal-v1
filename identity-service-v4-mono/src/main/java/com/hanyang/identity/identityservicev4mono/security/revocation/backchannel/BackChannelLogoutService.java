package com.hanyang.identity.identityservicev4mono.security.revocation.backchannel;

import com.hanyang.identity.identityservicev4mono.security.revocation.AccessRevocationProperties;
import com.hanyang.identity.identityservicev4mono.security.revocation.AccessRevocationStore;
import org.springframework.stereotype.Service;

@Service
public class BackChannelLogoutService {
  private final LogoutTokenVerifier logoutTokenVerifier;
  private final AccessRevocationStore revocationStore;
  private final AccessRevocationProperties revocationProperties;

  public BackChannelLogoutService(
      LogoutTokenVerifier logoutTokenVerifier,
      AccessRevocationStore revocationStore,
      AccessRevocationProperties revocationProperties) {
    this.logoutTokenVerifier = logoutTokenVerifier;
    this.revocationStore = revocationStore;
    this.revocationProperties = revocationProperties;
  }

  public void handle(String logoutToken) {
    VerifiedLogoutToken verified = logoutTokenVerifier.verify(logoutToken);

    revocationStore.revokeSession(verified.sessionId(), revocationProperties.sessionRetention());
  }
}
