package com.personal.identity.account.domain.enums;

import com.personal.shared.enums.StringCodeEnum;

public enum Gender implements StringCodeEnum {
  OTHER("O"),
  MALE("M"),
  FEMALE("F");
  private final String code;

  Gender(String code) {
    this.code = code;
  }

  @Override
  public String getCode() {
    return this.code.;
  }
}
