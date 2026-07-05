package com.personal.identity.account.exception;

public class DuplicateEmailException extends  RuntimeException{
    public DuplicateEmailException(String email) {
        super("Email already exists: " + email);
    }
}
