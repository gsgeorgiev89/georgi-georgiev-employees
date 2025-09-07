package com.ggeorgiev.employees.services;

import com.ggeorgiev.employees.entities.EmployeeProject;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStreamReader;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class CsvParserService {
  // Support multiple date formats
  private static final DateTimeFormatter[] DATE_FORMATTERS = {
      DateTimeFormatter.ofPattern("yyyy-MM-dd"),
      DateTimeFormatter.ofPattern("MM/dd/yyyy"),
      DateTimeFormatter.ofPattern("dd/MM/yyyy"),
      DateTimeFormatter.ofPattern("yyyy/MM/dd"),
      DateTimeFormatter.ofPattern("dd-MM-yyyy"),
      DateTimeFormatter.ofPattern("MM-dd-yyyy"),
      DateTimeFormatter.ofPattern("yyyy.MM.dd"),
      DateTimeFormatter.ofPattern("dd.MM.yyyy"),
      DateTimeFormatter.ofPattern("yyyyMMdd")
  };

  public List<EmployeeProject> parseCsvFile(MultipartFile file) throws IOException {
    List<EmployeeProject> employeeProjects = new ArrayList<>();

    try (CSVReader csvReader = new CSVReader(new InputStreamReader(file.getInputStream()))) {
      String[] record;
      boolean isFirstRow = true;

      while ((record = csvReader.readNext()) != null) {
        // Skip header row if necessary
        if (isFirstRow && !isNumeric(record[0].trim())) {
          isFirstRow = false;
          continue;
        }
        isFirstRow = false;

        if (record.length < 4) {
          log.warn("Skipping incomplete record");
          continue;
        }

        // Trim fields once
        for (int i = 0; i < record.length; i++) {
          record[i] = record[i].trim();
        }

        try {
          EmployeeProject employeeProject = parseRecord(record);
          if (employeeProject != null) {
            employeeProjects.add(employeeProject);
          }
        } catch (Exception e) {
          log.error("Error parsing record {}: {}", java.util.Arrays.toString(record), e.getMessage(), e);
        }
      }
    } catch (CsvException e) {
      throw new IOException("Error reading CSV file: " + e.getMessage(), e);
    }

    return employeeProjects;
  }

  private EmployeeProject parseRecord(String[] record) {
    Long empId = Long.parseLong(record[0].trim());
    Long projectId = Long.parseLong(record[1].trim());
    LocalDate dateFrom = parseDate(record[2].trim());
    LocalDate dateTo = parseDate(record[3].trim());

    // If dateTo is null, set it to today
    if (dateTo == null) {
      dateTo = LocalDate.now();
    }

    return new EmployeeProject(empId, projectId, dateFrom, dateTo);
  }

  private LocalDate parseDate(String dateString) {
    if (dateString == null || dateString.trim().isEmpty() ||
        "NULL".equalsIgnoreCase(dateString.trim()) ||
        "null".equals(dateString.trim())) {
      return null;
    }

    String cleanDateString = dateString.trim();

    // Try each formatter until one works
    for (DateTimeFormatter formatter : DATE_FORMATTERS) {
      try {
        return LocalDate.parse(cleanDateString, formatter);
      } catch (DateTimeParseException e) {
        // Continue to next formatter
      }
    }

    throw new IllegalArgumentException("Unable to parse date: " + dateString +
        ". Supported formats: yyyy-MM-dd, MM/dd/yyyy, dd/MM/yyyy, yyyy/MM/dd, dd-MM-yyyy, MM-dd-yyyy, yyyy.MM.dd, dd.MM.yyyy, yyyyMMdd");
  }

  private boolean isNumeric(String str) {
    try {
      Long.parseLong(str);
      return true;
    } catch (NumberFormatException e) {
      return false;
    }
  }
}