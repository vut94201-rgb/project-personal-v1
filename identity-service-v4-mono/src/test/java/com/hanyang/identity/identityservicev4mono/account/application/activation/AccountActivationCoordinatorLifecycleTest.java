package com.hanyang.identity.identityservicev4mono.account.application.activation;

import com.hanyang.identity.identityservicev4mono.account.domain.Account;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountId;
import com.hanyang.identity.identityservicev4mono.account.domain.AccountStatus;
import com.hanyang.identity.identityservicev4mono.employee.domain.EmployeeId;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.MockingDetails;
import org.springframework.beans.factory.ObjectProvider;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockingDetails;

/**
 * Characterization tests for the canonical Account PENDING -> ACTIVE lifecycle.
 *
 * <p>This test intentionally avoids Spring context, H2, LDAP, Keycloak, and physical outbox
 * infrastructure. Its boundary is the AccountActivationCoordinator itself.</p>
 *
 * <p>The coordinator is instantiated reflectively so the test remains focused on behavior even
 * if constructor wiring uses ObjectProvider to break provisioning-service cycles.</p>
 */
class AccountActivationCoordinatorLifecycleTest {

    @Test
    void directorySynchronizationAloneMustNotActivateAndMustScheduleKeycloak() {
        Harness harness = Harness.create(
                true,   // directory current
                false   // Keycloak current
        );

        invokeLifecycleCallback(
                harness.coordinator(),
                CallbackKind.DIRECTORY,
                harness.account().getId()
        );

        assertEquals(
                AccountStatus.PENDING,
                harness.account().getStatus(),
                "Directory synchronization alone must not activate the Account"
        );

        assertEquals(
                0,
                harness.accountSaveCount(),
                "PENDING Account must not be persisted as ACTIVE"
        );

        assertTrue(
                harness.identityProviderSynchronizationRequests() >= 1,
                "After directory synchronization, Keycloak synchronization must be requested"
        );
    }

    @Test
    void keycloakSynchronizationAloneMustNotActivate() {
        Harness harness = Harness.create(
                false,  // directory not current
                true    // Keycloak current
        );

        invokeLifecycleCallback(
                harness.coordinator(),
                CallbackKind.IDENTITY_PROVIDER,
                harness.account().getId()
        );

        assertEquals(
                AccountStatus.PENDING,
                harness.account().getStatus(),
                "Keycloak synchronization alone must not activate the Account"
        );

        assertEquals(
                0,
                harness.accountSaveCount(),
                "Account must remain PENDING until directory state is also current"
        );
    }

    @Test
    void bothProviderStatesCurrentActivateAccountAndEmitFreshSynchronization() {
        Harness harness = Harness.create(
                true,
                true
        );

        invokeLifecycleCallback(
                harness.coordinator(),
                CallbackKind.IDENTITY_PROVIDER,
                harness.account().getId()
        );

        assertEquals(
                AccountStatus.ACTIVE,
                harness.account().getStatus(),
                "Both current provider states are required before PENDING -> ACTIVE"
        );

        assertTrue(
                harness.accountSaveCount() >= 1,
                "Activated Account must be persisted"
        );

        assertTrue(
                harness.directorySynchronizationRequests() >= 1,
                "After activation the directory desired state must be re-emitted so authentication can be enabled"
        );

        assertTrue(
                harness.identityProviderSynchronizationRequests() >= 1,
                "After activation the Keycloak desired state must be re-emitted so the federated account can be enabled"
        );
    }

    @Test
    void alreadyActiveAccountMustNotBeActivatedAgain() {
        Harness harness = Harness.create(
                true,
                true
        );

        harness.account().activate();

        invokeLifecycleCallback(
                harness.coordinator(),
                CallbackKind.IDENTITY_PROVIDER,
                harness.account().getId()
        );

        assertEquals(
                AccountStatus.ACTIVE,
                harness.account().getStatus()
        );

        /*
         * The coordinator may still choose to repair/re-emit provider desired state,
         * but it must not perform another business activation transition.
         */
        assertTrue(
                harness.accountSaveCount() <= 1,
                "Coordinator must not repeatedly persist a second ACTIVE transition"
        );
    }

    private static void invokeLifecycleCallback(
            AccountActivationCoordinator coordinator,
            CallbackKind callbackKind,
            AccountId accountId
    ) {
        Method callback = findLifecycleCallback(
                coordinator.getClass(),
                callbackKind
        );

        try {
            callback.setAccessible(true);
            callback.invoke(
                    coordinator,
                    accountId
            );
        } catch (ReflectiveOperationException exception) {
            throw new AssertionError(
                    "Unable to invoke AccountActivationCoordinator lifecycle callback: "
                            + callback,
                    exception
            );
        }
    }

    private static Method findLifecycleCallback(
            Class<?> coordinatorType,
            CallbackKind callbackKind
    ) {
        for (Method method : coordinatorType.getDeclaredMethods()) {
            if (method.getParameterCount() != 1) {
                continue;
            }

            if (!method.getParameterTypes()[0].isAssignableFrom(AccountId.class)
                    && !AccountId.class.isAssignableFrom(method.getParameterTypes()[0])) {
                continue;
            }

            String name = method.getName().toLowerCase();

            boolean matches = switch (callbackKind) {
                case DIRECTORY ->
                        name.contains("directory");

                case IDENTITY_PROVIDER ->
                        name.contains("identity")
                                || name.contains("provider")
                                || name.contains("keycloak");
            };

            if (matches) {
                return method;
            }
        }

        throw new AssertionError(
                "No " + callbackKind
                        + " lifecycle callback taking AccountId was found on "
                        + coordinatorType.getName()
                        + ". Available methods: "
                        + java.util.Arrays.toString(coordinatorType.getDeclaredMethods())
        );
    }

    private enum CallbackKind {
        DIRECTORY,
        IDENTITY_PROVIDER
    }

    private static final class Harness {

        private final Account account;
        private final AccountActivationCoordinator coordinator;

        private final Map<String, Object> dependencyMocks;
        private final Object accountRepository;

        private Harness(
                Account account,
                AccountActivationCoordinator coordinator,
                Map<String, Object> dependencyMocks,
                Object accountRepository
        ) {
            this.account = account;
            this.coordinator = coordinator;
            this.dependencyMocks = dependencyMocks;
            this.accountRepository = accountRepository;
        }

        static Harness create(
                boolean directoryCurrent,
                boolean identityProviderCurrent
        ) {
            Account account = Account.create(
                    AccountId.newId(),
                    EmployeeId.newId(),
                    "nguyenvana"
            );

            Constructor<?> constructor = selectConstructor();

            Type[] genericParameterTypes =
                    constructor.getGenericParameterTypes();

            Class<?>[] parameterTypes =
                    constructor.getParameterTypes();

            Object[] arguments =
                    new Object[parameterTypes.length];

            Map<String, Object> dependencyMocks =
                    new HashMap<>();

            Object accountRepository = null;

            for (int index = 0; index < parameterTypes.length; index++) {

                Class<?> parameterType =
                        parameterTypes[index];

                Type genericType =
                        genericParameterTypes[index];

                Object dependency =
                        createDependency(
                                parameterType,
                                genericType,
                                account,
                                directoryCurrent,
                                identityProviderCurrent,
                                dependencyMocks
                        );

                arguments[index] = dependency;

                String dependencyName =
                        dependencyName(
                                parameterType,
                                genericType
                        );

                dependencyMocks.put(
                        dependencyName,
                        unwrapObjectProviderDependency(
                                dependency,
                                parameterType,
                                genericType
                        )
                );

                if (dependencyName.contains("AccountRepository")) {
                    accountRepository = dependency;
                }
            }

            assertNotNull(
                    accountRepository,
                    "AccountActivationCoordinator must depend on AccountRepository"
            );

            try {
                constructor.setAccessible(true);

                AccountActivationCoordinator coordinator =
                        (AccountActivationCoordinator) constructor.newInstance(
                                arguments
                        );

                return new Harness(
                        account,
                        coordinator,
                        dependencyMocks,
                        accountRepository
                );
            } catch (ReflectiveOperationException exception) {
                throw new AssertionError(
                        "Unable to instantiate AccountActivationCoordinator",
                        exception
                );
            }
        }

        Account account() {
            return account;
        }

        AccountActivationCoordinator coordinator() {
            return coordinator;
        }

        long accountSaveCount() {
            return invocationCount(
                    accountRepository,
                    "save"
            );
        }

        long directorySynchronizationRequests() {
            return synchronizationRequestCount(
                    "Directory"
            );
        }

        long identityProviderSynchronizationRequests() {
            long count =
                    synchronizationRequestCount(
                            "AccountProvisioningService"
                    );

            if (count > 0) {
                return count;
            }

            return synchronizationRequestCount(
                    "IdentityProvider"
            );
        }

        private long synchronizationRequestCount(
                String dependencyNameFragment
        ) {
            return dependencyMocks
                    .entrySet()
                    .stream()
                    .filter(entry ->
                            entry.getKey().contains(
                                    dependencyNameFragment
                            )
                    )
                    .filter(entry ->
                            entry.getValue() != null
                                    && mockingDetails(
                                    entry.getValue()
                            ).isMock()
                    )
                    .mapToLong(entry ->
                            invocationCount(
                                    entry.getValue(),
                                    "requestSynchronization"
                            )
                    )
                    .sum();
        }

        private static long invocationCount(
                Object mock,
                String methodName
        ) {
            MockingDetails details =
                    mockingDetails(mock);

            if (!details.isMock()) {
                return 0;
            }

            return details
                    .getInvocations()
                    .stream()
                    .filter(invocation ->
                            invocation
                                    .getMethod()
                                    .getName()
                                    .equals(methodName)
                    )
                    .count();
        }

        private static Constructor<?> selectConstructor() {
            Constructor<?>[] constructors =
                    AccountActivationCoordinator.class
                            .getDeclaredConstructors();

            if (constructors.length == 0) {
                throw new AssertionError(
                        "AccountActivationCoordinator has no constructor"
                );
            }

            Constructor<?> selected =
                    constructors[0];

            for (Constructor<?> constructor : constructors) {
                if (constructor.getParameterCount()
                        > selected.getParameterCount()) {
                    selected = constructor;
                }
            }

            return selected;
        }

        private static Object createDependency(
                Class<?> parameterType,
                Type genericType,
                Account account,
                boolean directoryCurrent,
                boolean identityProviderCurrent,
                Map<String, Object> dependencyMocks
        ) {

            if (ObjectProvider.class.isAssignableFrom(
                    parameterType
            )) {

                Class<?> providedType =
                        resolveProvidedType(
                                genericType
                        );

                Object providedMock =
                        createTypedMock(
                                providedType,
                                account,
                                directoryCurrent,
                                identityProviderCurrent
                        );

                dependencyMocks.put(
                        providedType.getSimpleName(),
                        providedMock
                );

                @SuppressWarnings("unchecked")
                ObjectProvider<Object> provider =
                        mock(
                                ObjectProvider.class,
                                invocation -> {
                                    String methodName =
                                            invocation
                                                    .getMethod()
                                                    .getName();

                                    if (methodName.equals("getIfAvailable")
                                            || methodName.equals("getObject")
                                            || methodName.equals("getIfUnique")) {
                                        return providedMock;
                                    }

                                    return Answers
                                            .RETURNS_DEFAULTS
                                            .answer(invocation);
                                }
                        );

                return provider;
            }

            return createTypedMock(
                    parameterType,
                    account,
                    directoryCurrent,
                    identityProviderCurrent
            );
        }

        private static Object createTypedMock(
                Class<?> type,
                Account account,
                boolean directoryCurrent,
                boolean identityProviderCurrent
        ) {

            String typeName =
                    type.getSimpleName();

            if (typeName.equals("AccountRepository")) {
                return mock(
                        type,
                        invocation -> {
                            String methodName =
                                    invocation
                                            .getMethod()
                                            .getName();

                            if (methodName.startsWith("find")
                                    && Optional.class.isAssignableFrom(
                                    invocation
                                            .getMethod()
                                            .getReturnType()
                            )) {
                                return Optional.of(account);
                            }

                            if (methodName.equals("save")) {
                                return invocation.getArgument(0);
                            }

                            return Answers
                                    .RETURNS_DEFAULTS
                                    .answer(invocation);
                        }
                );
            }

            boolean directoryDependency =
                    typeName.contains("Directory");

            boolean providerDependency =
                    typeName.contains("Provisioning")
                            || typeName.contains("IdentityProvider")
                            || typeName.contains("Keycloak");

            return mock(
                    type,
                    invocation -> {
                        Method method =
                                invocation.getMethod();

                        Class<?> returnType =
                                method.getReturnType();

                        boolean current =
                                directoryDependency
                                        ? directoryCurrent
                                        : providerDependency
                                        && identityProviderCurrent;

                        if (returnType == boolean.class
                                || returnType == Boolean.class) {
                            return current;
                        }

                        if (Optional.class.isAssignableFrom(
                                returnType
                        )) {
                            Class<?> optionalType =
                                    resolveOptionalType(
                                            method.getGenericReturnType()
                                    );

                            if (optionalType == null) {
                                return Optional.empty();
                            }

                            Object state =
                                    createStateMock(
                                            optionalType,
                                            current,
                                            directoryDependency
                                    );

                            return Optional.of(
                                    state
                            );
                        }

                        return Answers
                                .RETURNS_DEFAULTS
                                .answer(invocation);
                    }
            );
        }

        private static Object createStateMock(
                Class<?> stateType,
                boolean current,
                boolean directory
        ) {
            return mock(
                    stateType,
                    invocation -> {
                        Method method =
                                invocation.getMethod();

                        String methodName =
                                method
                                        .getName()
                                        .toLowerCase();

                        Class<?> returnType =
                                method.getReturnType();

                        if (returnType == boolean.class
                                || returnType == Boolean.class) {
                            if (methodName.contains("current")
                                    || methodName.contains("sync")) {
                                return current;
                            }

                            return false;
                        }

                        if (returnType == long.class
                                || returnType == Long.class) {
                            if (methodName.contains("desired")) {
                                return 1L;
                            }

                            if (methodName.contains("synced")) {
                                return current
                                        ? 1L
                                        : 0L;
                            }

                            return 0L;
                        }

                        if (returnType == int.class
                                || returnType == Integer.class) {
                            if (methodName.contains("desired")) {
                                return 1;
                            }

                            if (methodName.contains("synced")) {
                                return current
                                        ? 1
                                        : 0;
                            }

                            return 0;
                        }

                        if (returnType.isEnum()) {
                            Object matched =
                                    enumConstant(
                                            returnType,
                                            current
                                                    ? "SYNCED"
                                                    : "PENDING"
                                    );

                            if (matched != null
                                    && (methodName.contains("status")
                                    || methodName.contains("sync"))) {
                                return matched;
                            }

                            matched =
                                    enumConstant(
                                            returnType,
                                            directory
                                                    ? "DS389"
                                                    : "KEYCLOAK"
                                    );

                            if (matched != null) {
                                return matched;
                            }
                        }

                        return Answers
                                .RETURNS_DEFAULTS
                                .answer(invocation);
                    }
            );
        }

        private static Object enumConstant(
                Class<?> enumType,
                String constantName
        ) {
            for (Object constant :
                    enumType.getEnumConstants()) {

                if (((Enum<?>) constant)
                        .name()
                        .equals(constantName)) {
                    return constant;
                }
            }

            return null;
        }

        private static Class<?> resolveOptionalType(
                Type genericReturnType
        ) {
            if (!(genericReturnType
                    instanceof ParameterizedType parameterizedType)) {
                return null;
            }

            Type[] arguments =
                    parameterizedType
                            .getActualTypeArguments();

            if (arguments.length != 1) {
                return null;
            }

            if (arguments[0] instanceof Class<?> clazz) {
                return clazz;
            }

            return null;
        }

        private static Class<?> resolveProvidedType(
                Type genericType
        ) {
            if (!(genericType
                    instanceof ParameterizedType parameterizedType)) {
                throw new AssertionError(
                        "Unable to determine ObjectProvider generic type: "
                                + genericType
                );
            }

            Type[] arguments =
                    parameterizedType
                            .getActualTypeArguments();

            if (arguments.length != 1
                    || !(arguments[0]
                    instanceof Class<?> clazz)) {
                throw new AssertionError(
                        "Unable to determine ObjectProvider target: "
                                + genericType
                );
            }

            return clazz;
        }

        private static String dependencyName(
                Class<?> parameterType,
                Type genericType
        ) {
            if (ObjectProvider.class.isAssignableFrom(
                    parameterType
            )) {
                return resolveProvidedType(
                        genericType
                ).getSimpleName();
            }

            return parameterType.getSimpleName();
        }

        private static Object unwrapObjectProviderDependency(
                Object dependency,
                Class<?> parameterType,
                Type genericType
        ) {
            if (!ObjectProvider.class.isAssignableFrom(
                    parameterType
            )) {
                return dependency;
            }

            /*
             * The actual provided service mock was already registered
             * separately by createDependency().
             */
            return null;
        }
    }
}