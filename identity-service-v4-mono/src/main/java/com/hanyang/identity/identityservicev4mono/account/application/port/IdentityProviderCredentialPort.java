//package com.hanyang.identity.identityservicev4mono.account.application.port;
//
///**
// * Provider-neutral credential lifecycle operations.
// *
// * <p>Credentials never belong to the Account aggregate and must never be
// * persisted by the Identity Service.</p>
// */
//public interface IdentityProviderCredentialPort {
//
//    void setTemporaryPassword(
//            String externalId,
//            String temporaryPassword
//    );
//
//    /**
//     * Requests the identity provider to send its own password-setup action.
//     *
//     * @return {@code true} when the action was requested, or {@code false}
//     *         when the external identity has no email address to deliver to.
//     */
//    boolean sendPasswordSetupEmail(String externalId);
//}