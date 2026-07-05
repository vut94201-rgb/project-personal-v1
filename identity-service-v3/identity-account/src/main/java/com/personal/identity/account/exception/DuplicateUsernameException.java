package com.personal.identity.account.exception;

public class DuplicateUsernameException  extends  RuntimeException{

    public DuplicateUsernameException(String username) {
        super("Username already exists: " + username);
    }
}
