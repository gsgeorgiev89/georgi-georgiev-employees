package com.ggeorgiev.employees.services;

import com.ggeorgiev.employees.entities.EmployeeProject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvParserServiceTest {

    private CsvParserService csvParserService;

    @BeforeEach
    void setUp() {
        csvParserService = new CsvParserService();
    }

    @Test
    void testParseCsvFile_BasicFormat() throws IOException {
        String csvContent = "143,12,2013-11-01,2014-01-05\n" +
                           "218,10,2012-05-16,NULL\n" +
                           "143,10,2009-01-01,2011-04-27";
        
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.csv", "text/csv", csvContent.getBytes());

        List<EmployeeProject> result = csvParserService.parseCsvFile(file);

        assertEquals(3, result.size());
        
        EmployeeProject first = result.get(0);
        assertEquals(143L, first.getEmpId());
        assertEquals(12L, first.getProjectId());
        assertEquals(LocalDate.of(2013, 11, 1), first.getDateFrom());
        assertEquals(LocalDate.of(2014, 1, 5), first.getDateTo());
        
        EmployeeProject second = result.get(1);
        assertEquals(218L, second.getEmpId());
        assertEquals(LocalDate.now(), second.getDateTo()); // NULL should become today
    }

    @Test
    void testParseCsvFile_WithHeader() throws IOException {
        String csvContent = "EmpID,ProjectID,DateFrom,DateTo\n" +
                           "143,12,2013-11-01,2014-01-05\n" +
                           "218,10,2012-05-16,NULL";
        
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.csv", "text/csv", csvContent.getBytes());

        List<EmployeeProject> result = csvParserService.parseCsvFile(file);

        assertEquals(2, result.size()); // Header should be skipped
        assertEquals(143L, result.get(0).getEmpId());
        assertEquals(218L, result.get(1).getEmpId());
    }

    @Test
    void testParseCsvFile_DifferentDateFormats() throws IOException {
        String csvContent = "143,12,2013-11-01,2014-01-05\n" +
                           "218,10,05/16/2012,01/05/2014\n" +
                           "350,15,16/05/2012,05/01/2014\n" +
                           "100,20,2012/05/16,2014/01/05";
        
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.csv", "text/csv", csvContent.getBytes());

        List<EmployeeProject> result = csvParserService.parseCsvFile(file);

        assertEquals(4, result.size());
        
        // All should parse correctly with different formats
        assertEquals(LocalDate.of(2013, 11, 1), result.get(0).getDateFrom());
        assertEquals(LocalDate.of(2012, 5, 16), result.get(1).getDateFrom());
        assertEquals(LocalDate.of(2012, 5, 16), result.get(2).getDateFrom());
        assertEquals(LocalDate.of(2012, 5, 16), result.get(3).getDateFrom());
    }

    @Test
    void testParseCsvFile_EmptyFile() throws IOException {
        String csvContent = "";
        
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.csv", "text/csv", csvContent.getBytes());

        List<EmployeeProject> result = csvParserService.parseCsvFile(file);

        assertTrue(result.isEmpty());
    }

    @Test
    void testParseCsvFile_MalformedCsv() throws IOException {
        String csvContent = "143,12\n" +
                           "218,10,2012-05-16,NULL";
        
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.csv", "text/csv", csvContent.getBytes());

        List<EmployeeProject> result = csvParserService.parseCsvFile(file);

        assertEquals(1, result.size()); // Only the valid row should be processed
        assertEquals(218L, result.get(0).getEmpId());
    }

    @Test
    void testParseCsvFile_WithWhitespace() throws IOException {
        String csvContent = " 143 , 12 , 2013-11-01 , 2014-01-05 \n" +
                           " 218 , 10 , 2012-05-16 , NULL ";
        
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.csv", "text/csv", csvContent.getBytes());

        List<EmployeeProject> result = csvParserService.parseCsvFile(file);

        assertEquals(2, result.size());
        assertEquals(143L, result.get(0).getEmpId());
        assertEquals(12L, result.get(0).getProjectId());
    }

    @Test
    void testParseCsvFile_NullValues() throws IOException {
        String csvContent = "143,12,2013-11-01,null\n" +
                           "218,10,2012-05-16,\n" +
                           "350,15,2012-05-16,NULL";
        
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.csv", "text/csv", csvContent.getBytes());

        List<EmployeeProject> result = csvParserService.parseCsvFile(file);

        assertEquals(3, result.size());
        
        // All should have dateTo set to today
        assertEquals(LocalDate.now(), result.get(0).getDateTo());
        assertEquals(LocalDate.now(), result.get(1).getDateTo());
        assertEquals(LocalDate.now(), result.get(2).getDateTo());
    }
}