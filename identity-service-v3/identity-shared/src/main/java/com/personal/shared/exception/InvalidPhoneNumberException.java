package com.personal.shared.exception;

public class InvalidPhoneNumberException extends RuntimeException {

    public InvalidPhoneNumberException(String phoneNumber) {
        super("Invalid phone number: " + phoneNumber);
    }

    public InvalidPhoneNumberException(
            String phoneNumber,
            Throwable cause) {
        super("Invalid phone number: " + phoneNumber, cause);
    }
}