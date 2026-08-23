package com.hanyang.identity.identityservicev4mono.account.domain;

import java.util.Objects;
import java.util.UUID;

public record AccountId(UUID value) {

    public AccountId {
        Objects.requireNonNull(value, "value must not be null");
    }

    public static AccountId newId() {
        return new AccountId(UUID.randomUUID());
    }
}