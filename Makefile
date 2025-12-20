.PHONY: build test run-local

# Build the backend application
build-local-be:
	cd backend && ./gradlew spotlessApply compileKotlin compileTestKotlin build

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
