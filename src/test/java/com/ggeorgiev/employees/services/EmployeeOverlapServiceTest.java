package com.ggeorgiev.employees.services;

import com.ggeorgiev.employees.entities.EmployeePairResponse;
import com.ggeorgiev.employees.entities.EmployeeProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EmployeeOverlapServiceTest {

    private EmployeeService employeeOverlapService;

    @BeforeEach
    void setUp() {
        employeeOverlapService = new EmployeeService();
    }

    @Test
    void testFindLongestWorkingPair_BasicCase() {
        List<EmployeeProject> projects = Arrays.asList(
                new EmployeeProject(143L, 12L, LocalDate.of(2013, 11, 1), LocalDate.of(2014, 1, 5)),
                new EmployeeProject(218L, 12L, LocalDate.of(2013, 12, 1), LocalDate.of(2014, 2, 1)),
                new EmployeeProject(143L, 10L, LocalDate.of(2012, 1, 1), LocalDate.of(2014, 4, 27)),
                new EmployeeProject(218L, 10L, LocalDate.of(2012, 5, 16), LocalDate.of(2014, 1, 5))
        );

        EmployeePairResponse result = employeeOverlapService.findLongestWorkingPair(projects);

        assertNotNull(result);
        assertEquals(143L, result.getEmployee1Id());
        assertEquals(218L, result.getEmployee2Id());
        assertTrue(result.getTotalDays() > 0);
        assertEquals(2, result.getProjectOverlaps().size());
    }

    @Test
    void testFindLongestWorkingPair_NoOverlap() {
        List<EmployeeProject> projects = Arrays.asList(
                new EmployeeProject(143L, 12L, LocalDate.of(2013, 1, 1), LocalDate.of(2013, 6, 1)),
                new EmployeeProject(218L, 12L, LocalDate.of(2013, 7, 1), LocalDate.of(2013, 12, 1))
        );

      EmployeePairResponse result = employeeOverlapService.findLongestWorkingPair(projects);

        assertNull(result);
    }

    @Test
    void testFindLongestWorkingPair_SingleProject() {
        List<EmployeeProject> projects = Arrays.asList(
                new EmployeeProject(143L, 12L, LocalDate.of(2014, 11, 1), LocalDate.of(2014, 11, 5)),
                new EmployeeProject(218L, 12L, LocalDate.of(2014, 11, 1), LocalDate.of(2014, 11, 4))
        );

      EmployeePairResponse result = employeeOverlapService.findLongestWorkingPair(projects);

        assertNotNull(result);
        assertEquals(143L, result.getEmployee1Id());
        assertEquals(218L, result.getEmployee2Id());
        assertEquals(4L, result.getTotalDays());
        assertEquals(1, result.getProjectOverlaps().size());
        assertEquals(12L, result.getProjectOverlaps().get(0).getProjectId());
    }

    @Test
    void testFindLongestWorkingPair_ThreeEmployees() {
        List<EmployeeProject> projects = Arrays.asList(
                new EmployeeProject(143L, 12L, LocalDate.of(2013, 1, 1), LocalDate.of(2013, 12, 31)),
                new EmployeeProject(218L, 12L, LocalDate.of(2013, 6, 1), LocalDate.of(2013, 12, 31)),
                new EmployeeProject(350L, 12L, LocalDate.of(2013, 9, 1), LocalDate.of(2013, 12, 31))
        );

      EmployeePairResponse result = employeeOverlapService.findLongestWorkingPair(projects);

        assertNotNull(result);
        // Should be pair 143 and 218 with 214 days (June 1 to Dec 31)
        assertTrue((result.getEmployee1Id() == 143L && result.getEmployee2Id() == 218L) ||
                   (result.getEmployee1Id() == 218L && result.getEmployee2Id() == 143L));
        assertEquals(214L, result.getTotalDays());
    }

    @Test
    void testFindLongestWorkingPair_ExactSamePeriod() {
        List<EmployeeProject> projects = Arrays.asList(
                new EmployeeProject(143L, 12L, LocalDate.of(2013, 1, 1), LocalDate.of(2013, 1, 31)),
                new EmployeeProject(218L, 12L, LocalDate.of(2013, 1, 1), LocalDate.of(2013, 1, 31))
        );

      EmployeePairResponse result = employeeOverlapService.findLongestWorkingPair(projects);

        assertNotNull(result);
        assertEquals(31L, result.getTotalDays());
    }

    @Test
    void testFindAllWorkingPairs() {
        List<EmployeeProject> projects = Arrays.asList(
                new EmployeeProject(143L, 12L, LocalDate.of(2013, 1, 1), LocalDate.of(2013, 6, 1)),
                new EmployeeProject(218L, 12L, LocalDate.of(2013, 3, 1), LocalDate.of(2013, 8, 1)),
                new EmployeeProject(350L, 12L, LocalDate.of(2013, 5, 1), LocalDate.of(2013, 10, 1))
        );

        List<EmployeePairResponse> results = employeeOverlapService.findAllWorkingPairs(projects);

        assertNotNull(results);
        assertEquals(3, results.size()); // All possible pairs: 143-218, 143-350, 218-350
        
        // Results should be sorted by total days descending
        for (int i = 0; i < results.size() - 1; i++) {
            assertTrue(results.get(i).getTotalDays() >= results.get(i + 1).getTotalDays());
        }
    }

    @Test
    void testFindLongestWorkingPair_EmptyList() {
        List<EmployeeProject> projects = Arrays.asList();

      EmployeePairResponse result = employeeOverlapService.findLongestWorkingPair(projects);

        assertNull(result);
    }

    @Test
    void testFindLongestWorkingPair_SingleEmployee() {
        List<EmployeeProject> projects = Arrays.asList(
                new EmployeeProject(143L, 12L, LocalDate.of(2013, 1, 1), LocalDate.of(2013, 6, 1))
        );

      EmployeePairResponse result = employeeOverlapService.findLongestWorkingPair(projects);

        assertNull(result); // Can't have pairs with only one employee
    }
}