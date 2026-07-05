package com.personal.identity.account.exception;

public class IdentityUserNotFoundException extends RuntimeException{


    public IdentityUserNotFoundException(Long id) {
        super("Identity user not found with id: " + id);
    }
}
