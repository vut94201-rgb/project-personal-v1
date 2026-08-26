package com.hanyang.identity.identityservicev4mono.account.application.port;



/**
 * Provider-neutral credential write port for the account directory.
 *
 * <p>The directory is the credential source of truth. Raw password values are
 * transient transport data only: callers must never persist or log them.</p>
 */
public interface DirectoryCredentialPort {

    /**
     * Replaces the directory password for the account identified by username.
     * The directory server is responsible for hashing and storing the value.
     */
    void setPassword(String username, String rawPassword);
}