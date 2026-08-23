package com.hanyang.identity.identityservicev4mono.account.application.port;

public interface IdentityProviderAccountPort {
    void disableUser(String subject);

    String createUser(String username);


}