.PHONY: build test run-local

# Build the backend application
build-local-be:
	cd backend && ./gradlew spotlessApply spotlessKotlinGradle compileKotlin compileTestKotlin build

# Run tests locally
test-local:
	cd backend && ./gradlew test

# Run tests with JaCoCo coverage report
test-local-coverage:
	cd backend && ./gradlew test jacocoTestReport
	@echo "Coverage report generated at backend/build/reports/jacoco/test/html/index.html"

# Run tests with coverage verification (fails if coverage < threshold)
test-local-coverage-verify:
	cd backend && ./gradlew test jacocoTestReport jacocoTestCoverageVerification

# Run the application locally with dependencies
run-local:
	@echo "Starting local infrastructure..."
	cd localdev && docker-compose up -d
	@echo "Waiting for Kafka to be ready..."
	@sleep 5
	@echo "Starting backend application..."
	cd backend && SPRING_PROFILES_ACTIVE=local ./gradlew bootRun

# Kill process using port 8080
kill-8080:
	@echo "Checking for process on port 8080..."
	@lsof -ti :8080 | xargs -r kill -9 || true
	@echo "Port 8080 is free."

# Stop local environment and cleanup
down-local: kill-8080
	@echo "Stopping local infrastructure..."
	cd localdev && docker-compose down
	@echo "Local environment stopped."
