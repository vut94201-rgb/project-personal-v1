package com.hanyang.identity.identityservicev4mono.account.infrastructure.persistence.converter;

import com.hanyang.identity.identityservicev4mono.account.domain.AccountStatus;
import com.hanyang.identity.identityservicev4mono.shared.persistence.converter.AbstractStringCodeEnumConverter;
import jakarta.persistence.Converter;

@Converter
public class AccountStatusConverter
        extends AbstractStringCodeEnumConverter<AccountStatus> {

    public AccountStatusConverter() {
        super(AccountStatus.class);
    }
}