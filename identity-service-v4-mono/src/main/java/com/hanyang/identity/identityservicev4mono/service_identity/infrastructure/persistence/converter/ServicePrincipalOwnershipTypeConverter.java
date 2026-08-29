package com.hanyang.identity.identityservicev4mono.service_identity.infrastructure.persistence.converter;

import com.hanyang.identity.identityservicev4mono.service_identity.domain.ServicePrincipalOwnershipType;
import com.hanyang.identity.identityservicev4mono.shared.persistence.converter.AbstractStringCodeEnumConverter;
import jakarta.persistence.Converter;
@Converter(autoApply = false)
public class ServicePrincipalOwnershipTypeConverter
        extends AbstractStringCodeEnumConverter<ServicePrincipalOwnershipType> {

    public ServicePrincipalOwnershipTypeConverter() {
        super(ServicePrincipalOwnershipType.class);
    }
}