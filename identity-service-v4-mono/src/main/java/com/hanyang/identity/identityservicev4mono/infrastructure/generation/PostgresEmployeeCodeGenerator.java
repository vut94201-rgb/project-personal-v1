package com.hanyang.identity.identityservicev4mono.infrastructure.generation;

import com.hanyang.identity.identityservicev4mono.employee.application.port.EmployeeCodeGenerator;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class PostgresEmployeeCodeGenerator implements EmployeeCodeGenerator {

  private static final String NEXT_SEQUENCE_SQL = "SELECT nextval('employee_code_seq')";
  private static final String EMPLOYEE_CODE_FORMAT = "HY%06d";
  private final JdbcTemplate jdbcTemplate;

  public PostgresEmployeeCodeGenerator(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  @Override
  public String nextCode() {
    Long sequence = jdbcTemplate.queryForObject(NEXT_SEQUENCE_SQL, Long.class);
    if (sequence == null) {
      throw new IllegalStateException("Employee code sequence did not return a value");
    }

    return EMPLOYEE_CODE_FORMAT.formatted(sequence);
  }
}
