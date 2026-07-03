package com.personal.identity.account.domain.enums;

import com.personal.enums.StringCodeEnum;

public enum UserStatus implements StringCodeEnum {
  ACTIVE("A"),
  INACTIVE("I"),
  LOCKED("L"),
  PENDING("P");

  private final String code;

  UserStatus(String code) {
    this.code = code;
  }

  @Override
  public String getCode() {
    return this.code;
  }
}
