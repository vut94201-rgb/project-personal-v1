package com.hanyang.identity.identityservicev4mono.employee.application.port;

/**
 * Generates the human-facing employee code used by the current demo onboarding flow.
 *
 * <p>The Employee aggregate keeps its UUID as the technical identifier. This port exists so the
 * temporary demo strategy can later be replaced by an HR-provided employee code without changing
 * the onboarding use case.
 */
public interface EmployeeCodeGenerator {

  String nextCode();
}
