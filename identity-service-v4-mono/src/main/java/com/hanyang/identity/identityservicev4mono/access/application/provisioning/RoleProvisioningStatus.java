package com.hanyang.identity.identityservicev4mono.access.application.provisioning;

public enum RoleProvisioningStatus {
    PENDING,
    SYNCING,
    SYNCED,
    FAILED,
    DRIFTED
}