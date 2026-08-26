package com.hanyang.identity.identityservicev4mono.account.application.credential;


import com.hanyang.identity.identityservicev4mono.account.application.exception.AccountCredentialEmailUnavailableException;
import com.hanyang.identity.identityservicev4mono.account.application.exception.AccountCredentialOnboardingNotAllowedException;
import com.hanyang.identity.identityservicev4mono.account.application.port.DirectoryCredentialPort;
import com.hanyang.identity.identityservicev4mono.account.application.port.IdentityProviderCredentialActionPort;

import com.hanyang.identity.identityservicev4mono.account.application.provisioning.AccountProvisioningState;
import com.hanyang.identity.identityservicev4mono.account.application.provisioning.AccountProvisioningStateRepository;
import com.hanyang.identity.identityservicev4mono.account.domain.Account;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountId;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountRepository;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountStatus;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import com.hanyang.identity.identityservicev4mono.shared.identityprovider.IdentityProviderType;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;


class AccountCredentialOnboardingServiceTest {

    @Test
    void temporaryPasswordIsWrittenToDirectoryAndPasswordChangeIsRequiredInProvider() {
        Account account = account(AccountStatus.ACTIVE);
        StubDirectoryCredentialPort directoryPort = new StubDirectoryCredentialPort();
        StubCredentialActionPort actionPort = new StubCredentialActionPort();
        AccountCredentialOnboardingService service = service(
                account,
                directoryPort,
                actionPort,
                currentBinding(account.getId(), "keycloak-subject")
        );

        TemporaryPasswordOnboardingResult result =
                service.issueTemporaryPassword(account.getId());

        assertNotNull(result.temporaryPassword());
        assertEquals("emp001", directoryPort.username);
        assertEquals(result.temporaryPassword(), directoryPort.password);
        assertEquals("keycloak-subject", actionPort.requiredActionExternalId);
        assertEquals(AccountStatus.ACTIVE, account.getStatus());
    }

    @Test
    void directoryPasswordIsNotChangedWhenRequiredActionCannotBeInstalled() {
        Account account = account(AccountStatus.ACTIVE);
        StubDirectoryCredentialPort directoryPort = new StubDirectoryCredentialPort();
        StubCredentialActionPort actionPort = new StubCredentialActionPort();
        actionPort.failRequiredAction = true;
        AccountCredentialOnboardingService service = service(
                account,
                directoryPort,
                actionPort,
                currentBinding(account.getId(), "keycloak-subject")
        );

        assertThrows(
                IllegalStateException.class,
                () -> service.issueTemporaryPassword(account.getId())
        );

        assertNull(directoryPort.password);
    }

    @Test
    void pendingAccountCannotInitializeCredentials() {
        Account account = account(AccountStatus.PENDING);
        StubDirectoryCredentialPort directoryPort = new StubDirectoryCredentialPort();
        StubCredentialActionPort actionPort = new StubCredentialActionPort();
        AccountCredentialOnboardingService service = service(
                account,
                directoryPort,
                actionPort,
                null
        );

        assertThrows(
                AccountCredentialOnboardingNotAllowedException.class,
                () -> service.issueTemporaryPassword(account.getId())
        );

        assertNull(directoryPort.password);
        assertNull(actionPort.requiredActionExternalId);
    }

    @Test
    void activeAccountWithoutCurrentKeycloakBindingCannotInitializeCredentials() {
        Account account = account(AccountStatus.ACTIVE);
        StubDirectoryCredentialPort directoryPort = new StubDirectoryCredentialPort();
        StubCredentialActionPort actionPort = new StubCredentialActionPort();
        AccountCredentialOnboardingService service = service(
                account,
                directoryPort,
                actionPort,
                null
        );

        assertThrows(
                AccountCredentialOnboardingNotAllowedException.class,
                () -> service.issueTemporaryPassword(account.getId())
        );
    }

    @Test
    void setupEmailRequiresEmailOnExternalIdentity() {
        Account account = account(AccountStatus.ACTIVE);
        StubDirectoryCredentialPort directoryPort = new StubDirectoryCredentialPort();
        StubCredentialActionPort actionPort = new StubCredentialActionPort();
        actionPort.emailAvailable = false;
        AccountCredentialOnboardingService service = service(
                account,
                directoryPort,
                actionPort,
                currentBinding(account.getId(), "keycloak-subject")
        );

        assertThrows(
                AccountCredentialEmailUnavailableException.class,
                () -> service.sendPasswordSetupEmail(account.getId())
        );
    }

    @Test
    void setupEmailDelegatesToProviderWhenEmailExists() {
        Account account = account(AccountStatus.ACTIVE);
        StubDirectoryCredentialPort directoryPort = new StubDirectoryCredentialPort();
        StubCredentialActionPort actionPort = new StubCredentialActionPort();
        actionPort.emailAvailable = true;
        AccountCredentialOnboardingService service = service(
                account,
                directoryPort,
                actionPort,
                currentBinding(account.getId(), "keycloak-subject")
        );

        assertDoesNotThrow(
                () -> service.sendPasswordSetupEmail(account.getId())
        );

        assertEquals("keycloak-subject", actionPort.emailExternalId);
    }

    private static AccountCredentialOnboardingService service(
            Account account,
            StubDirectoryCredentialPort directoryPort,
            StubCredentialActionPort actionPort,
            AccountProvisioningState binding
    ) {
        AccountProvisioningStateRepository stateRepository =
                mock(AccountProvisioningStateRepository.class);
        when(stateRepository.findByAccountIdAndProvider(
                account.getId(),
                IdentityProviderType.KEYCLOAK
        )).thenReturn(Optional.ofNullable(binding));

        return new AccountCredentialOnboardingService(
                new SingleAccountRepository(account),
                stateRepository,
                directoryPort,
                actionPort,
                new TemporaryPasswordGenerator()
        );
    }

    private static Account account(AccountStatus status) {
        return Account.rehydrate(
                AccountId.newId(),
                new EmployeeId(UUID.randomUUID()),
                "emp001",
                status
        );
    }

    private static AccountProvisioningState currentBinding(
            AccountId accountId,
            String externalId
    ) {
        AccountProvisioningState state = AccountProvisioningState.pending(
                accountId,
                IdentityProviderType.KEYCLOAK
        );
        long revision = state.beginSynchronization();
        state.markSynchronized(
                revision,
                externalId,
                "emp001",
                Instant.parse("2026-08-26T04:00:00Z")
        );
        return state;
    }

    private static final class StubDirectoryCredentialPort
            implements DirectoryCredentialPort {

        private String username;
        private String password;

        @Override
        public void setPassword(
                String username,
                String rawPassword
        ) {
            this.username = username;
            this.password = rawPassword;
        }
    }

    private static final class StubCredentialActionPort
            implements IdentityProviderCredentialActionPort {

        private String requiredActionExternalId;
        private String emailExternalId;
        private boolean emailAvailable;
        private boolean failRequiredAction;

        @Override
        public void requirePasswordChange(String externalId) {
            if (failRequiredAction) {
                throw new IllegalStateException("provider unavailable");
            }
            this.requiredActionExternalId = externalId;
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