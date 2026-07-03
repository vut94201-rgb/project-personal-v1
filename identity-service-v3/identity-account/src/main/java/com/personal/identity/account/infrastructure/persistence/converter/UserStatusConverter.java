package com.personal.identity.account.infrastructure.persistence.converter;

import com.personal.identity.account.domain.enums.UserStatus;
import com.personal.identity.jpa.support.converter.AbstractStringCodeEnumConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class UserStatusConverter extends AbstractStringCodeEnumConverter<UserStatus> {
  public UserStatusConverter() {
    super(UserStatus.class);
  }
}
