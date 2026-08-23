package com.hanyang.identity.identityservicev4mono.access.application.provisioning;


import com.hanyang.identity.identityservicev4mono.access.domain.RoleId;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountId;

import java.util.UUID;

public record AccountRoleProvisioningKey(
        AccountId accountId,
        RoleId roleId
) {
    private static final String DELIMITER = ":";

    public String serialize() {
        return accountId.value() + DELIMITER + roleId.value();
    }

    public static AccountRoleProvisioningKey parse(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Account-role aggregate id must not be blank");
        }

        String[] parts = value.trim().split(DELIMITER, -1);
        if (parts.length != 2) {
            throw new IllegalArgumentException(
                    "Invalid account-role aggregate id: " + value
            );
        }

        try {
            return new AccountRoleProvisioningKey(
                    new AccountId(UUID.fromString(parts[0])),
                    new RoleId(UUID.fromString(parts[1]))
            );
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException(
                    "Invalid account-role aggregate id: " + value,
                    exception
            );
        }
    }
}