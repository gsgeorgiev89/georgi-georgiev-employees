package com.ggeorgiev.employees.services;

import com.ggeorgiev.employees.entities.EmployeePair;
import com.ggeorgiev.employees.entities.EmployeePairResponse;
import com.ggeorgiev.employees.entities.EmployeeProject;
import com.ggeorgiev.employees.entities.ProjectOverlap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class EmployeeService {
  public EmployeePairResponse findLongestWorkingPair(List<EmployeeProject> employeeProjects) {
    log.info("Finding longest working employee pair from {} projects", employeeProjects.size());

    Map<EmployeePair, Long> pairOverlapDays = new HashMap<>();
    Map<EmployeePair, List<ProjectOverlap>> pairProjectOverlaps = new HashMap<>();

    Map<Long, List<EmployeeProject>> projectGroups = employeeProjects.stream()
        .collect(Collectors.groupingBy(EmployeeProject::getProjectId));
    log.debug("Grouped projects into {} groups", projectGroups.size());

    for (Map.Entry<Long, List<EmployeeProject>> entry : projectGroups.entrySet()) {
      Long projectId = entry.getKey();
      List<EmployeeProject> projectEmployees = entry.getValue();
      log.debug("Processing project {} with {} employees assigned", projectId, projectEmployees.size());

      for (int i = 0; i < projectEmployees.size(); i++) {
        for (int j = i + 1; j < projectEmployees.size(); j++) {
          EmployeeProject emp1 = projectEmployees.get(i);
          EmployeeProject emp2 = projectEmployees.get(j);

          long overlapDays = calculateOverlapDays(emp1, emp2);

          if (overlapDays > 0) {
            EmployeePair pairKey = getPair(emp1, emp2);
            pairOverlapDays.merge(pairKey, overlapDays, Long::sum);

            ProjectOverlap projectOverlap = new ProjectOverlap(emp1.getEmpId(), emp2.getEmpId(), projectId, overlapDays);
            pairProjectOverlaps.computeIfAbsent(pairKey, k -> new ArrayList<>()).add(projectOverlap);

            log.debug("Pair {} and {} overlap on project {} for {} days", emp1.getEmpId(), emp2.getEmpId(), projectId, overlapDays);
          }
        }
      }
    }

    Optional<Map.Entry<EmployeePair, Long>> maxOverlapEntry = pairOverlapDays.entrySet().stream()
        .max(Map.Entry.comparingByValue());

    if (maxOverlapEntry.isPresent()) {
      EmployeePair pair = maxOverlapEntry.get().getKey();
      Long totalDays = maxOverlapEntry.get().getValue();
      List<ProjectOverlap> projectOverlaps = pairProjectOverlaps.get(pair);

      log.info("Longest working pair found: {} and {} with total {} days across {} projects",
          pair.getEmp1(), pair.getEmp2(), totalDays, projectOverlaps.size());

      return new EmployeePairResponse(pair.getEmp1(), pair.getEmp2(), totalDays, projectOverlaps);
    } else {
      log.info("No overlapping employee pairs found");
      return null;
    }
  }

  private static EmployeePair getPair(EmployeeProject emp1, EmployeeProject emp2) {
    var empId1 = emp1.getEmpId();
    var empId2 = emp2.getEmpId();
    if (empId1.compareTo(empId2) < 0) {
      return new EmployeePair(emp1.getEmpId(), emp2.getEmpId());
    } else {
      return new EmployeePair(emp2.getEmpId(), emp1.getEmpId());
    }
  }

  private long calculateOverlapDays(EmployeeProject emp1, EmployeeProject emp2) {
    LocalDate start1 = emp1.getDateFrom();
    LocalDate end1 = emp1.getDateTo();
    LocalDate start2 = emp2.getDateFrom();
    LocalDate end2 = emp2.getDateTo();

    LocalDate overlapStart = start1.isAfter(start2) ? start1 : start2;
    LocalDate overlapEnd = end1.isBefore(end2) ? end1 : end2;

    if (overlapStart.isAfter(overlapEnd)) {
      log.trace("No overlap between employee {} and {} for projects", emp1.getEmpId(), emp2.getEmpId());
      return 0;
    }

    long days = ChronoUnit.DAYS.between(overlapStart, overlapEnd) + 1;
    log.trace("Overlap between employee {} and {} is {} days", emp1.getEmpId(), emp2.getEmpId(), days);
    return days;
  }

  public List<EmployeePairResponse> findAllWorkingPairs(List<EmployeeProject> employeeProjects) {
    log.info("Finding all working pairs from {} projects", employeeProjects.size());

    Map<EmployeePair, Long> pairOverlapDays = new HashMap<>();
    Map<EmployeePair, List<ProjectOverlap>> pairProjectOverlaps = new HashMap<>();

    Map<Long, List<EmployeeProject>> projectGroups = employeeProjects.stream()
        .collect(Collectors.groupingBy(EmployeeProject::getProjectId));
    log.debug("Grouped projects into {} groups", projectGroups.size());

    for (Map.Entry<Long, List<EmployeeProject>> entry : projectGroups.entrySet()) {
      Long projectId = entry.getKey();
      List<EmployeeProject> projectEmployees = entry.getValue();
      log.debug("Processing project {} with {} employees assigned", projectId, projectEmployees.size());

      for (int i = 0; i < projectEmployees.size(); i++) {
        for (int j = i + 1; j < projectEmployees.size(); j++) {
          EmployeeProject emp1 = projectEmployees.get(i);
          EmployeeProject emp2 = projectEmployees.get(j);

          long overlapDays = calculateOverlapDays(emp1, emp2);

          if (overlapDays > 0) {
            EmployeePair pairKey = getPair(emp1, emp2);
            pairOverlapDays.merge(pairKey, overlapDays, Long::sum);

            ProjectOverlap projectOverlap = new ProjectOverlap(emp1.getEmpId(), emp2.getEmpId(), projectId, overlapDays);
            pairProjectOverlaps.computeIfAbsent(pairKey, k -> new ArrayList<>()).add(projectOverlap);

            log.debug("Pair {} and {} overlap on project {} for {} days", emp1.getEmpId(), emp2.getEmpId(), projectId, overlapDays);
          }
        }
      }
    }

    List<EmployeePairResponse> allPairs = pairOverlapDays.entrySet().stream()
        .map(entry -> {
          EmployeePair pair = entry.getKey();
          Long totalDays = entry.getValue();
          List<ProjectOverlap> projectOverlaps = pairProjectOverlaps.get(pair);
          return new EmployeePairResponse(pair.getEmp1(), pair.getEmp2(), totalDays, projectOverlaps);
        })
        .sorted((a, b) -> b.getTotalDays().compareTo(a.getTotalDays()))
        .collect(Collectors.toList());

    log.info("Found {} employee pairs with overlapping days", allPairs.size());

    return allPairs;
  }
}
