package com.personal.identity.account.domain.enums;

public enum UserStatus implements com.personal.identity.jpa.support.converter.StringCodeEnum {
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
