package com.hanyang.identity.identityservicev4mono.account.application.port;

public interface IdentityProviderCredentialPolicyPort {


    void clearPasswordChangeRequirement(String externalId);
}