package com.hanyang.identity.identityservicev4mono.account.application.provisioning;
public enum AccountProvisioningStatus {
    PENDING,
    SYNCING,
    SYNCED,
    FAILED,
    DRIFTED
}