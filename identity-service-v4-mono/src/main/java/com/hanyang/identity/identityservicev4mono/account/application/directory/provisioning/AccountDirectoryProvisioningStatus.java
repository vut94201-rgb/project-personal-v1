package com.hanyang.identity.identityservicev4mono.account.application.directory.provisioning;


public enum AccountDirectoryProvisioningStatus {
    PENDING,
    SYNCING,
    SYNCED,
    FAILED,
    DRIFTED
}