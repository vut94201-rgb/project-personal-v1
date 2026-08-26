package com.hanyang.identity.identityservicev4mono.account.application.credential;


import com.hanyang.identity.identityservicev4mono.account.application.exception.AccountCredentialEmailUnavailableException;
import com.hanyang.identity.identityservicev4mono.account.application.exception.AccountCredentialOnboardingNotAllowedException;
import com.hanyang.identity.identityservicev4mono.account.application.port.IdentityProviderCredentialPort;
import com.hanyang.identity.identityservicev4mono.account.domain.Account;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountId;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountRepository;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountStatus;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AccountCredentialOnboardingServiceTest {

    @Test
    void temporaryPasswordIsSentToProviderAndNeverAddedToAccountState() {
        Account account = account(AccountStatus.ACTIVE, "keycloak-subject");
        StubCredentialPort credentialPort = new StubCredentialPort();
        AccountCredentialOnboardingService service = service(account, credentialPort);

        TemporaryPasswordOnboardingResult result =
                service.issueTemporaryPassword(account.getId());

        assertNotNull(result.temporaryPassword());
        assertEquals(result.temporaryPassword(), credentialPort.temporaryPassword);
        assertEquals("keycloak-subject", credentialPort.externalId);

        // The aggregate remains purely business identity state: no credential mutation.
        assertEquals(AccountStatus.ACTIVE, account.getStatus());
        assertEquals("keycloak-subject", account.getKeycloakSubject());
    }

    @Test
    void pendingAccountCannotInitializeCredentials() {
        Account account = account(AccountStatus.PENDING, null);
        StubCredentialPort credentialPort = new StubCredentialPort();
        AccountCredentialOnboardingService service = service(account, credentialPort);

        assertThrows(
                AccountCredentialOnboardingNotAllowedException.class,
                () -> service.issueTemporaryPassword(account.getId())
        );

        assertNull(credentialPort.temporaryPassword);
    }

    @Test
    void setupEmailRequiresEmailOnExternalIdentity() {
        Account account = account(AccountStatus.ACTIVE, "keycloak-subject");
        StubCredentialPort credentialPort = new StubCredentialPort();
        credentialPort.emailAvailable = false;
        AccountCredentialOnboardingService service = service(account, credentialPort);

        assertThrows(
                AccountCredentialEmailUnavailableException.class,
                () -> service.sendPasswordSetupEmail(account.getId())
        );
    }

    @Test
    void setupEmailDelegatesToProviderWhenEmailExists() {
        Account account = account(AccountStatus.ACTIVE, "keycloak-subject");
        StubCredentialPort credentialPort = new StubCredentialPort();
        credentialPort.emailAvailable = true;
        AccountCredentialOnboardingService service = service(account, credentialPort);

        assertDoesNotThrow(
                () -> service.sendPasswordSetupEmail(account.getId())
        );

        assertEquals("keycloak-subject", credentialPort.emailExternalId);
    }

    private static AccountCredentialOnboardingService service(
            Account account,
            StubCredentialPort credentialPort
    ) {
        return new AccountCredentialOnboardingService(
                new SingleAccountRepository(account),
                credentialPort,
                new TemporaryPasswordGenerator()
        );
    }

    private static Account account(
            AccountStatus status,
            String keycloakSubject
    ) {
        return Account.rehydrate(
                AccountId.newId(),
                new EmployeeId(UUID.randomUUID()),
                "emp001",
                keycloakSubject,
                status
        );
    }

    private static final class StubCredentialPort
            implements IdentityProviderCredentialPort {

        private String externalId;
        private String temporaryPassword;
        private String emailExternalId;
        private boolean emailAvailable;

        @Override
        public void setTemporaryPassword(
                String externalId,
                String temporaryPassword
        ) {
            this.externalId = externalId;
            this.temporaryPassword = temporaryPassword;
        }

        @Override
        public boolean sendPasswordSetupEmail(String externalId) {
            this.emailExternalId = externalId;
            return emailAvailable;
        }
    }

    private static final class SingleAccountRepository
            implements AccountRepository {

        private final Account account;

        private SingleAccountRepository(Account account) {
            this.account = account;
        }

        @Override
        public Account save(Account account) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Account> findById(AccountId id) {
            return account.getId().equals(id)
                    ? Optional.of(account)
                    : Optional.empty();
        }

        @Override
        public Optional<Account> findByUsername(String username) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Account> findByEmployeeId(EmployeeId employeeId) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean existsByUsername(String username) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean existsByEmployeeId(EmployeeId employeeId) {
            throw new UnsupportedOperationException();
        }
    }
}