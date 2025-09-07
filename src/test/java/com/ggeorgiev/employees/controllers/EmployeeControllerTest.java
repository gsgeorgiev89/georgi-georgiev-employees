package com.ggeorgiev.employees.controllers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ggeorgiev.employees.entities.EmployeePairResponse;
import com.ggeorgiev.employees.entities.EmployeeProject;
import com.ggeorgiev.employees.services.CsvParserService;
import com.ggeorgiev.employees.services.EmployeeService;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CsvParserService csvParserService;

    @MockitoBean
    private EmployeeService employeeService;

  @Test
  void testUploadCsvFile_Success() throws Exception {
    List<EmployeeProject> mockProjects = List.of(
        new EmployeeProject(143L, 12L, LocalDate.parse("2013-11-01"), LocalDate.parse("2014-01-05")),
        new EmployeeProject(218L, 10L, LocalDate.parse("2012-05-16"), null)
    );
    when(csvParserService.parseCsvFile(any())).thenReturn(mockProjects);

    EmployeePairResponse longestPair = new EmployeePairResponse(
        143L, 218L, 30L, List.of()
    );

    when(employeeService.findLongestWorkingPair(any())).thenReturn(longestPair);
    when(employeeService.findAllWorkingPairs(any())).thenReturn(List.of(longestPair));

    MockMultipartFile file = new MockMultipartFile(
        "file", "test.csv", "text/csv", "143,12,2013-11-01,2014-01-05\n218,10,2012-05-16,NULL".getBytes()
    );

    mockMvc.perform(multipart("/api/employees/upload").file(file))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.longestPair.employee1Id").value(143))
        .andExpect(jsonPath("$.longestPair.employee2Id").value(218));
  }

    @Test
    void testUploadCsvFile_EmptyFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.csv", "text/csv", new byte[0]);

        mockMvc.perform(multipart("/api/employees/upload")
                .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Please select a file to upload"));
    }

    @Test
    void testUploadCsvFile_NonCsvFile() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.txt", "text/plain", "some content".getBytes());

        mockMvc.perform(multipart("/api/employees/upload")
                .file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error").value("Please upload a CSV file"));
    }
}