package com.hanyang.identity.identityservicev4mono.organization.infrastructure.persistence.converter;

import com.hanyang.identity.identityservicev4mono.organization.domain.OrganizationReferenceStatus;
import com.hanyang.identity.identityservicev4mono.shared.persistence.converter.AbstractStringCodeEnumConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class OrganizationReferenceStatusConverter
        extends AbstractStringCodeEnumConverter<OrganizationReferenceStatus> {

    public OrganizationReferenceStatusConverter() {
        super(OrganizationReferenceStatus.class);
    }
}
