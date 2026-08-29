package com.hanyang.identity.identityservicev4mono.service_identity.application.provisioning;

public enum ServicePrincipalProvisioningStatus {
    PENDING,
    SYNCING,
    SYNCED,
    FAILED,
    DRIFTED
}