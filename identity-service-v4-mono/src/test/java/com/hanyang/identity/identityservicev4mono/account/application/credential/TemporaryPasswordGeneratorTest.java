package com.hanyang.identity.identityservicev4mono.account.application.credential;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TemporaryPasswordGeneratorTest {

    private final TemporaryPasswordGenerator generator =
            new TemporaryPasswordGenerator();

    @Test
    void generatedPasswordHasExpectedLengthAndCharacterClasses() {
        for (int i = 0; i < 100; i++) {
            String password = generator.generate();

            assertEquals(20, password.length());
            assertTrue(password.chars().anyMatch(Character::isUpperCase));
            assertTrue(password.chars().anyMatch(Character::isLowerCase));
            assertTrue(password.chars().anyMatch(Character::isDigit));
            assertTrue(password.chars().anyMatch(value ->
                    "!@#$%^&*_-+=".indexOf(value) >= 0
            ));
            assertTrue(password.chars().noneMatch(Character::isWhitespace));
        }
    }
}