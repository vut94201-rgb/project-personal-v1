package com.hanyang.identity.identityservicev4mono.account.application.credential;


import org.springframework.stereotype.Component;

import java.security.SecureRandom;

@Component
public class InitialPasswordGenerator {

    private static final int LENGTH = 20;

    private static final char[] UPPER = "ABCDEFGHJKLMNPQRSTUVWXYZ".toCharArray();
    private static final char[] LOWER = "abcdefghijkmnopqrstuvwxyz".toCharArray();
    private static final char[] DIGITS = "23456789".toCharArray();
    private static final char[] SPECIAL = "!@#$%^&*_-+=".toCharArray();
    private static final char[] ALL =
            (new String(UPPER)
                    + new String(LOWER)
                    + new String(DIGITS)
                    + new String(SPECIAL))
                    .toCharArray();

    private final SecureRandom secureRandom = new SecureRandom();

    public String generate() {
        char[] password = new char[LENGTH];

        // Guarantee a useful baseline even when the directory password policy
        // requires multiple character classes.
        password[0] = randomFrom(UPPER);
        password[1] = randomFrom(LOWER);
        password[2] = randomFrom(DIGITS);
        password[3] = randomFrom(SPECIAL);

        for (int i = 4; i < password.length; i++) {
            password[i] = randomFrom(ALL);
        }

        shuffle(password);
        return new String(password);
    }

    private char randomFrom(char[] source) {
        return source[secureRandom.nextInt(source.length)];
    }

    private void shuffle(char[] value) {
        for (int i = value.length - 1; i > 0; i--) {
            int j = secureRandom.nextInt(i + 1);
            char tmp = value[i];
            value[i] = value[j];
            value[j] = tmp;
        }
    }
}