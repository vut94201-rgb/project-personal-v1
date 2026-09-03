package com.hanyang.identity.identityservicev4mono.security.revocation.backchannel;

public interface LogoutTokenVerifier {

    VerifiedLogoutToken verify(String logoutToken);
}