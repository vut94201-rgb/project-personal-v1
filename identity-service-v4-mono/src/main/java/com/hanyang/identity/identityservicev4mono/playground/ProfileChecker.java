package com.hanyang.identity.identityservicev4mono.playground;

import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class ProfileChecker {

    public ProfileChecker(Environment environment) {
        System.out.println(
                "Active profiles: "
                        + Arrays.toString(environment.getActiveProfiles())
        );
    }
}