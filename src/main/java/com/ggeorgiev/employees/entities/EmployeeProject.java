package com.ggeorgiev.employees.entities;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EmployeeProject {
  private Long empId;
  private Long projectId;
  private LocalDate dateFrom;
  private LocalDate dateTo;
}
