//package com.hanyang.identity.identityservicev4mono.infrastructure.keycloak.account;
//
//
//import com.hanyang.identity.identityservicev4mono.account.application.port.IdentityProviderCredentialPort;
//import com.hanyang.identity.identityservicev4mono.infrastructure.keycloak.config.KeycloakProperties;
//import com.hanyang.identity.identityservicev4mono.infrastructure.keycloak.exception.KeycloakIntegrationException;
//import jakarta.ws.rs.ProcessingException;
//import jakarta.ws.rs.WebApplicationException;
//import lombok.RequiredArgsConstructor;
//import org.keycloak.admin.client.Keycloak;
//import org.keycloak.admin.client.resource.UserResource;
//import org.keycloak.representations.idm.CredentialRepresentation;
//import org.keycloak.representations.idm.UserRepresentation;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//
//@Component
//@RequiredArgsConstructor
//public class KeycloakCredentialAdapter
//        implements IdentityProviderCredentialPort {
//
//    private static final String UPDATE_PASSWORD = "UPDATE_PASSWORD";
//
//    private final Keycloak keycloakAdminClient;
//    private final KeycloakProperties properties;
//
//    @Override
//    public void setTemporaryPassword(
//            String externalId,
//            String temporaryPassword
//    ) {
//        String normalizedExternalId = requireText(
//                externalId,
//                "Keycloak user id"
//        );
//        String normalizedPassword = requireText(
//                temporaryPassword,
//                "temporaryPassword"
//        );
//
//        try {
//            CredentialRepresentation credential =
//                    new CredentialRepresentation();
//            credential.setType(CredentialRepresentation.PASSWORD);
//            credential.setValue(normalizedPassword);
//            credential.setTemporary(true);
//
//            user(normalizedExternalId).resetPassword(credential);
//        } catch (ProcessingException exception) {
//            throw unableToConnect(exception);
//        } catch (WebApplicationException exception) {
//            throw new KeycloakIntegrationException(
//                    "Unable to set temporary Keycloak password. HTTP "
//                            + exception.getResponse().getStatus(),
//                    exception
//            );
//        } catch (RuntimeException exception) {
//            if (exception instanceof KeycloakIntegrationException integrationException) {
//                throw integrationException;
//            }
//            throw new KeycloakIntegrationException(
//                    "Unable to set temporary Keycloak password",
//                    exception
//            );
//        }
//    }
//
//    @Override
//    public boolean sendPasswordSetupEmail(String externalId) {
//        String normalizedExternalId = requireText(
//                externalId,
//                "Keycloak user id"
//        );
//
//        try {
//            UserResource userResource = user(normalizedExternalId);
//            UserRepresentation representation = userResource.toRepresentation();
//
//            if (representation.getEmail() == null
//                    || representation.getEmail().isBlank()) {
//                return false;
//            }
//
//            userResource.executeActionsEmail(
//                    List.of(UPDATE_PASSWORD)
//            );
//            return true;
//        } catch (ProcessingException exception) {
//            throw unableToConnect(exception);
//        } catch (WebApplicationException exception) {
//            throw new KeycloakIntegrationException(
//                    "Unable to send Keycloak password setup email. HTTP "
//                            + exception.getResponse().getStatus(),
//                    exception
//            );
//        } catch (RuntimeException exception) {
//            if (exception instanceof KeycloakIntegrationException integrationException) {
//                throw integrationException;
//            }
//            throw new KeycloakIntegrationException(
//                    "Unable to send Keycloak password setup email",
//                    exception
//            );
//        }
//    }
//
//    private UserResource user(String externalId) {
//        return keycloakAdminClient
//                .realm(requireText(properties.realm(), "integration.keycloak.realm"))
//                .users()
//                .get(externalId);
//    }
//
//    private static KeycloakIntegrationException unableToConnect(
//            ProcessingException exception
//    ) {
//        return new KeycloakIntegrationException(
//                "Unable to connect to Keycloak Admin API",
//                exception
//        );
//    }
//
//    private static String requireText(
//            String value,
//            String fieldName
//    ) {
//        if (value == null || value.isBlank()) {
//            throw new KeycloakIntegrationException(
//                    fieldName + " must not be blank"
//            );
//        }
//        return value.trim();
//    }
//}