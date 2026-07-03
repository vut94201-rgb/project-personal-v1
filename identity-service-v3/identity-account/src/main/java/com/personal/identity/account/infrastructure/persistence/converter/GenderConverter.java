package com.personal.identity.account.infrastructure.persistence.converter;

import com.personal.identity.account.domain.enums.Gender;
import com.personal.identity.jpa.support.converter.AbstractStringCodeEnumConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class GenderConverter extends AbstractStringCodeEnumConverter<Gender> {
  protected GenderConverter() {
    super(Gender.class);
  }
}
