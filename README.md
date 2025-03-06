# School Schedule Manager

A Spring Boot application for managing school schedules, including teachers, students, subjects, and classrooms.

## Features

- Automatic schedule generation for the entire school
- Validation of scheduling requirements:
    - Teachers must have at least one subject
    - Students must have exactly 9 subjects
    - Subjects must have at least one teacher
- Multiple report formats:
    - Teacher schedules (by teacher)
    - Student schedules (by student and class)
    - Classroom schedules (by room)
- Excel reports with clear weekly layouts
- Interactive API documentation with Swagger UI

## Prerequisites

- Java 17 or higher
- Maven 3.8 or higher
- PostgreSQL 12 or higher
- Docker and Docker Compose (for containerized deployment)

## Running with Docker Compose

The easiest way to run the application is using Docker Compose:

1. Make sure Docker and Docker Compose are installed on your system

1.2. Add file .env to the root of the project

```env
DB_HOST=localhost
DB_PORT=5432
DB_NAME=scheduler
DB_SCHEMA=public
DB_USERNAME=postgres
DB_PASSWORD=postgres_secret
```

2. Run the application stack:

```bash
docker compose up -d
```

This will start:

- PostgreSQL database container
- Application container
- All necessary networking and volume mounts

The application will be available at:

- Main application: `http://localhost:8080`
- Swagger UI: `http://localhost:8080/swagger-ui.html`
- OpenAPI documentation: `http://localhost:8080/v3/api-docs`

To stop the application:

```bash
docker compose down
```

To view logs:

```bash
docker compose logs -f
```

## API Documentation

The API documentation is available in two formats:

1. Swagger UI (Interactive):
    - URL: `http://localhost:8080/swagger-ui.html`
    - Features:
        - Interactive API testing
        - Request/response examples
        - Schema documentation
        - Authentication support

2. OpenAPI Specification (Raw):
    - URL: `http://localhost:8080/v3/api-docs`
    - Format: JSON
    - Useful for generating client code

### API Endpoints

#### Schedule Generation

- `POST /api/schedule/generate` - Generate a new schedule for the current week

#### Reports

- `GET /api/reports/teacher-schedule` - Download teacher schedule report
- `GET /api/reports/student-schedule` - Download student schedule report
- `GET /api/reports/classroom-schedule` - Download classroom schedule report

#### Data Management

- `GET /api/teachers` - List all teachers
- `GET /api/students` - List all students
- `GET /api/subjects` - List all subjects
- `GET /api/classrooms` - List all classrooms

## Initial Data

The application comes with initial data loaded through Flyway migrations:

- Sample teachers with their subjects
- Sample students with their assigned subjects
- Sample classrooms with capacities
- Subject definitions with levels and weekly lesson counts

## Report Formats

All schedule reports are provided in Excel format with a consistent layout:

- Week header showing the date range
- Day headers (Monday through Friday)
- Time slots (8:00 - 17:00)
- Detailed lesson information including:
    - Subject name and level
    - Teacher name
    - Classroom
    - Student list (where applicable)

## Validation Rules

The system enforces several validation rules:

1. Each teacher must be assigned at least one subject
2. Each student must have exactly 9 subjects
3. Each subject must have at least one assigned teacher
4. Classroom capacity must not be exceeded
5. No scheduling conflicts for teachers, students, or classrooms

## Error Handling

The application provides clear error messages for various scenarios:

- Validation failures
- Scheduling conflicts
- Missing resources
- Database errors

## Contributing

1. Fork the repository
2. Create a feature branch
3. Commit your changes
4. Push to the branch
5. Create a Pull Request