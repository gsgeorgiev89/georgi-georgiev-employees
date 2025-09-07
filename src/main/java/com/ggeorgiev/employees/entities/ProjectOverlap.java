package com.ggeorgiev.employees.entities;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProjectOverlap {
  private Long employee1Id;
  private Long employee2Id;
  private Long projectId;
  private Long daysWorked;
}