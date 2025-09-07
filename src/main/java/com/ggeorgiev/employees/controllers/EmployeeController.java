package com.ggeorgiev.employees.controllers;

import com.ggeorgiev.employees.entities.EmployeePairResponse;
import com.ggeorgiev.employees.entities.EmployeeProject;
import com.ggeorgiev.employees.services.CsvParserService;
import com.ggeorgiev.employees.services.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/employees")
@CrossOrigin(origins = "*")
@Slf4j
public class EmployeeController {

  @Autowired
  CsvParserService csvParserService;

  @Autowired
  EmployeeService employeeService;

  @PostMapping("/upload")
  public ResponseEntity<?> uploadCsvFile(@RequestParam("file") MultipartFile file) {
    log.info("Received request to upload CSV file: {}", file.getOriginalFilename());

    try {
      if (file.isEmpty()) {
        log.warn("Uploaded file is empty");
        return ResponseEntity.badRequest()
            .body(createErrorResponse("Please select a file to upload"));
      }

      if (!file.getOriginalFilename().toLowerCase().endsWith(".csv")) {
        log.warn("Uploaded file is not a CSV: {}", file.getOriginalFilename());
        return ResponseEntity.badRequest()
            .body(createErrorResponse("Please upload a CSV file"));
      }

      List<EmployeeProject> employeeProjects = csvParserService.parseCsvFile(file);
      log.info("Parsed {} employee project records from CSV file", employeeProjects.size());

      if (employeeProjects.isEmpty()) {
        log.warn("No valid data found in the CSV file after parsing");
        return ResponseEntity.badRequest()
            .body(createErrorResponse("No valid data found in the CSV file"));
      }

      EmployeePairResponse longestPair = employeeService.findLongestWorkingPair(employeeProjects);

      if (longestPair == null) {
        log.info("No overlapping employee pairs found in data");
        return ResponseEntity.ok(createErrorResponse("No overlapping employee pairs found"));
      }

      List<EmployeePairResponse> allPairs = employeeService.findAllWorkingPairs(employeeProjects);
      log.info("Found {} employee pairs with overlaps", allPairs.size());

      Map<String, Object> response = new HashMap<>();
      response.put("success", true);
      response.put("longestPair", longestPair);
      response.put("allPairs", allPairs);
      response.put("totalRecords", employeeProjects.size());

      return ResponseEntity.ok(response);

    } catch (Exception e) {
      log.error("Error processing uploaded CSV file", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(createErrorResponse("Error processing file: " + e.getMessage()));
    }
  }

  private Map<String, Object> createErrorResponse(String message) {
    Map<String, Object> response = new HashMap<>();
    response.put("success", false);
    response.put("error", message);
    return response;
  }
}
