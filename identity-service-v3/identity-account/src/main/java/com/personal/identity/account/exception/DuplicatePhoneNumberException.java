package com.personal.identity.account.exception;

public class DuplicatePhoneNumberException extends RuntimeException {

  public DuplicatePhoneNumberException(String phoneNumber) {

    super("Phone number already exists");
  }
}
