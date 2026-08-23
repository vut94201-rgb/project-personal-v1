package com.hanyang.identity.identityservicev4mono.access.infrastructure.persistence.converter;

import com.hanyang.identity.identityservicev4mono.access.domain.RoleStatus;
import com.hanyang.identity.identityservicev4mono.shared.persistence.converter.AbstractStringCodeEnumConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class RoleStatusConverter
        extends AbstractStringCodeEnumConverter<RoleStatus> {

    public RoleStatusConverter() {
        super(RoleStatus.class);
    }
}