package com.hanyang.identity.identityservicev4mono.service_identity.infrastructure.persistence.converter;

import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalStatus;
import com.hanyang.identity.identityservicev4mono.shared.persistence.converter.AbstractStringCodeEnumConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class ServicePrincipalStatusConverter
        extends AbstractStringCodeEnumConverter<ServicePrincipalStatus> {

    public ServicePrincipalStatusConverter() {
        super(ServicePrincipalStatus.class);
    }
}