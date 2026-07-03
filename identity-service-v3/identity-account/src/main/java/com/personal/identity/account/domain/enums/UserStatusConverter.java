package com.personal.identity.account.domain.enums;

import com.personal.identity.account.infrastructure.AbstractStringCodeEnumConverter;
import jakarta.persistence.Converter;

import java.util.Map;

@Converter(autoApply = true)
public class UserStatusConverter extends AbstractStringCodeEnumConverter<UserStatus> {
  public UserStatusConverter() {
    super(UserStatus.class);
  }
}
