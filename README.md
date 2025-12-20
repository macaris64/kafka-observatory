# kafka-observatory

Kafka observatory is a tool for monitoring and analyzing Kafka clusters.

## How to run tests

```bash
./gradlew test
```

Test reports: `backend/build/reports/tests/test/index.html`

## Code Coverage

We use Jacoco for code coverage. A minimum of **70% line coverage** is enforced in the CI.

To generate a coverage report locally:
```bash
./gradlew jacocoTestReport
```
Coverage reports: `backend/build/reports/jacoco/test/html/index.html`

## API Documentation

The project uses OpenAPI 3 for automatic API documentation.

When the application is running, you can access:
- **Swagger UI**: `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI JSON**: `http://localhost:8080/v3/api-docs`
