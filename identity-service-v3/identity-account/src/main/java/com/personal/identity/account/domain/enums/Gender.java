package com.personal.identity.account.domain.enums;

public enum Gender implements com.personal.identity.jpa.support.converter.StringCodeEnum {
  OTHER("O"),
  MALE("M"),
  FEMALE("F");
  private final String code;

  Gender(String code) {
    this.code = code;
  }

  @Override
  public String getCode() {
    return this.code;
  }
}
