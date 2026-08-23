package com.hanyang.identity.identityservicev4mono.employee.infrastructure.persistence;

import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeStatus;
import com.hanyang.identity.identityservicev4mono.shared.persistence.converter.AbstractStringCodeEnumConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class EmployeeStatusConverter
        extends AbstractStringCodeEnumConverter<EmployeeStatus> {

    public EmployeeStatusConverter() {
        super(EmployeeStatus.class);
    }
}