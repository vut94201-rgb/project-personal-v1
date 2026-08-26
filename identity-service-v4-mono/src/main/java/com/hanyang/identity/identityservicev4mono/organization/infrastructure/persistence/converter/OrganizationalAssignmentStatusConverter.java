package com.hanyang.identity.identityservicev4mono.organization.infrastructure.persistence.converter;

import com.hanyang.identity.identityservicev4mono.organization.domain.OrganizationalAssignmentStatus;
import com.hanyang.identity.identityservicev4mono.shared.persistence.converter.AbstractStringCodeEnumConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = false)
public class OrganizationalAssignmentStatusConverter
        extends AbstractStringCodeEnumConverter<OrganizationalAssignmentStatus> {

    public OrganizationalAssignmentStatusConverter() {
        super(OrganizationalAssignmentStatus.class);
    }
}
