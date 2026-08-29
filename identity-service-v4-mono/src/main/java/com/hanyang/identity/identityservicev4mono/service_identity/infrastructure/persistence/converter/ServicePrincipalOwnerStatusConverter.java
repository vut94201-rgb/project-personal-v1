package com.hanyang.identity.identityservicev4mono.service_identity.infrastructure.persistence.converter;


import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalOwnerStatus;
import com.hanyang.identity.identityservicev4mono.shared.persistence.converter.AbstractStringCodeEnumConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class ServicePrincipalOwnerStatusConverter
        extends AbstractStringCodeEnumConverter<ServicePrincipalOwnerStatus> {

    public ServicePrincipalOwnerStatusConverter() {
        super(ServicePrincipalOwnerStatus.class);
    }
}