package com.ggeorgiev.employees.entities;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class EmployeePairResponse {
  private Long employee1Id;
  private Long employee2Id;
  private Long totalDays;
  private List<ProjectOverlap> projectOverlaps;
}
