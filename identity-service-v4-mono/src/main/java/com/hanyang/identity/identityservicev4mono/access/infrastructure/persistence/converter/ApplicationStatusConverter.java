package com.hanyang.identity.identityservicev4mono.access.infrastructure.persistence.converter;

import com.hanyang.identity.identityservicev4mono.access.domain.ApplicationStatus;
import com.hanyang.identity.identityservicev4mono.shared.persistence.converter.AbstractStringCodeEnumConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class ApplicationStatusConverter
        extends AbstractStringCodeEnumConverter<ApplicationStatus> {

    public ApplicationStatusConverter() {
        super(ApplicationStatus.class);
    }
}