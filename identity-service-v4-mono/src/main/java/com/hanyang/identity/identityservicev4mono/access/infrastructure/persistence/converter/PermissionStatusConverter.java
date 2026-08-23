package com.hanyang.identity.identityservicev4mono.access.infrastructure.persistence.converter;

import com.hanyang.identity.identityservicev4mono.access.domain.PermissionStatus;
import com.hanyang.identity.identityservicev4mono.shared.persistence.converter.AbstractStringCodeEnumConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class PermissionStatusConverter
        extends AbstractStringCodeEnumConverter<PermissionStatus> {

    public PermissionStatusConverter() {
        super(PermissionStatus.class);
    }
}