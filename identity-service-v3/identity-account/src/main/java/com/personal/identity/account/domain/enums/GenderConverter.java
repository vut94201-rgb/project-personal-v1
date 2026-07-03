package com.personal.identity.account.domain.enums;

import com.personal.identity.account.infrastructure.AbstractStringCodeEnumConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class GenderConverter extends AbstractStringCodeEnumConverter<Gender> {
  protected GenderConverter() {
    super(Gender.class);
  }
}
