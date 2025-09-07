# Employee Overlap Calculator

A Spring Boot application that processes CSV files containing employee project data and calculates pairs of employees who have worked together the longest on common projects.

---

## Features

- Upload CSV files with employee project records.
- Supports various common date formats in CSV (e.g., yyyy-MM-dd, MM/dd/yyyy, dd/MM/yyyy).
- Calculates the pair of employees with the longest total overlap period across projects.
- Lists all employee pairs with their overlapping projects and durations.
- Provides REST API endpoints to upload files and get results.
- Uses Thymeleaf with Bootstrap for a clean and responsive frontend UI.
- Detailed logging for traceability of file processing and calculations.

---

## Getting Started

### Prerequisites

- Java 17 or higher
- Maven 3.8+
- (Optional) IDE such as IntelliJ IDEA or Eclipse

### Running the Application

1. Clone the repository:

git clone https://github.com/gsgeorgiev89/georgi-georgiev-employees.git

2. Build and run with Maven:

mvn spring-boot:run

3. Access the application at:

http://localhost:8080/

4. Use the web UI or the REST API to upload CSV files and view employee overlap results.

---

## API Endpoint

- `POST /api/employees/upload` — Upload CSV file, returns JSON with longest working pair, all pairs, and metadata.

---

## CSV File Format

The uploaded CSV file should contain rows with the following columns:

| EmpID | ProjectID | DateFrom  | DateTo   |
|-------|------------|-----------|----------|
| Long  | Long       | Date      | Date or NULL|

- Supported date formats include:
- yyyy-MM-dd
- MM/dd/yyyy
- dd/MM/yyyy
- yyyy/MM/dd
- dd-MM-yyyy
- MM-dd-yyyy
- yyyy.MM.dd
- dd.MM.yyyy
- yyyyMMdd

- Use `NULL` or empty fields for ongoing projects (`DateTo`).

---

## Testing

The project includes unit and integration tests using:

- JUnit 5
- Spring Boot Test with `@WebMvcTest` for controllers
- Mockito for mocking service dependencies

Run tests with:

mvn test

---

## Logging

Detailed logs are available for CSV parsing, project grouping, overlap calculations, and controller request processing to aid debugging.

---

## Technologies Used

- Spring Boot 3.4+
- Thymeleaf Template Engine
- OpenCSV for CSV parsing
- Maven for build and dependency management
- Bootstrap 5 for styling
- Mockito and JUnit 5 for testing

---

## Future Improvements

- Support Bulk Uploads with Asynchronous Processing:
Enable uploading large CSV files in bulk while processing them asynchronously in the background. This improves user experience by making uploads non-blocking and scalable for large datasets.

- Integrate Database Persistence:
Store employee project data and calculated overlaps in a database for persistent storage, efficient querying, and historical reporting. This also enables incremental or partial updates without reprocessing entire files.

- Combine Both Approaches:
Implement bulk uploads processed asynchronously, with results stored in a database. This architecture supports high throughput and reliable data management, improving scalability and maintainability of the service.

- Enhance error reporting with detailed validation feedback
