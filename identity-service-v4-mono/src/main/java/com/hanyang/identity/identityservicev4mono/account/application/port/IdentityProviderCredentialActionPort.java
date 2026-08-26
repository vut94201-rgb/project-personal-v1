package com.hanyang.identity.identityservicev4mono.account.application.port;

/**
 * Provider-neutral identity-provider actions related to credential UX.
 *
 * <p>The identity provider does not own the password value. It only owns
 * authentication-facing actions such as forcing a password change or sending
 * a password-setup action email.</p>
 */
public interface IdentityProviderCredentialActionPort {

    /**
     * Marks the external identity so the user must change the password through
     * the identity-provider flow on the next authentication.
     */
    void requirePasswordChange(String externalId);

    /**
     * Requests the identity provider to send its own password-setup action.
     *
     * @return {@code true} when the action was requested, or {@code false}
     *         when the external identity has no email address to deliver to.
     */
    boolean sendPasswordSetupEmail(String externalId);
}