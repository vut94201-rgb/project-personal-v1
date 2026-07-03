package com.personal.identity.configuration.time;

import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class TimeConfiguration {
  public Clock clock() {
    return Clock.systemUTC();
  }
}
