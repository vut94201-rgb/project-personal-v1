package com.hanyang.identity.identityservicev4mono.access.application.port;

public interface IdentityProviderApplicationPort {

  ProvisionedApplication ensureApplication(
      String applicationCode, String applicationName, boolean enabled);

  record ProvisionedApplication(String externalId, String externalCode) {}
}
