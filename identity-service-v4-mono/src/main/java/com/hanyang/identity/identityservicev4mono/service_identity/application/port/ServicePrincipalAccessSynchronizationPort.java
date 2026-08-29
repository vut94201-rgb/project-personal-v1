package com.hanyang.identity.identityservicev4mono.service_identity.application.port;


import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalId;

/**
 * Application-level hook used by service-identity lifecycle/provisioning to
 * request synchronization of access assignments owned by the Access module.
 *
 * <p>This is intentionally provider-neutral. It prevents the service-identity
 * module from depending directly on Access implementation classes or Keycloak
 * role APIs.</p>
 */
public interface ServicePrincipalAccessSynchronizationPort {

    void requestAssignedRolesSynchronization(ServicePrincipalId servicePrincipalId);
}