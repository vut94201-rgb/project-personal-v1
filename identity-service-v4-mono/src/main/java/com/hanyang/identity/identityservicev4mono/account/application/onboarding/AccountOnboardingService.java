package com.hanyang.identity.identityservicev4mono.account.application.onboarding;

import com.hanyang.identity.identityservicev4mono.account.application.AccountCommandService;
import com.hanyang.identity.identityservicev4mono.account.application.AccountQueryService;
import com.hanyang.identity.identityservicev4mono.employee.application.EmployeeCommandService;
import com.hanyang.identity.identityservicev4mono.employee.application.EmployeeQueryService;
import com.hanyang.identity.identityservicev4mono.organization.application.CrewCommandService;
import com.hanyang.identity.identityservicev4mono.organization.application.CrewQueryService;
import com.hanyang.identity.identityservicev4mono.organization.application.DepartmentCommandService;
import com.hanyang.identity.identityservicev4mono.organization.application.DepartmentQueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AccountOnboardingService {

  private final AccountCommandService accountCommandService;
  private final AccountQueryService accountQueryService;
  private final EmployeeQueryService employeeQueryService;
  private final EmployeeCommandService employeeCommandService;
  private final CrewCommandService crewCommandService;
  private final CrewQueryService crewQueryService;
  private final DepartmentQueryService departmentQueryService;
  private final DepartmentCommandService departmentCommandService;

  public AccountOnboardingService(
      AccountCommandService accountCommandService,
      AccountQueryService accountQueryService,
      EmployeeQueryService employeeQueryService,
      EmployeeCommandService employeeCommandService,
      CrewCommandService crewCommandService,
      CrewQueryService crewQueryService,
      DepartmentQueryService departmentQueryService,
      DepartmentCommandService departmentCommandService) {
    this.accountCommandService = accountCommandService;
    this.accountQueryService = accountQueryService;
    this.employeeQueryService = employeeQueryService;
    this.employeeCommandService = employeeCommandService;
    this.crewCommandService = crewCommandService;
    this.crewQueryService = crewQueryService;
    this.departmentQueryService = departmentQueryService;
    this.departmentCommandService = departmentCommandService;
  }
}
