.PHONY: build test run-local

# Build the backend application
build-local-be:
	cd backend && ./gradlew spotlessApply compileKotlin compileTestKotlin build

# Run backend tests
test-local-be:
	cd backend && ./gradlew spotlessApply compileKotlin compileTestKotlin test

# Run the application locally with dependencies
run-local:
	@echo "Starting local infrastructure..."
	cd localdev && docker-compose up -d
	@echo "Waiting for Kafka to be ready..."
	@sleep 5
	@echo "Starting backend application..."
	cd backend && SPRING_PROFILES_ACTIVE=local ./gradlew bootRun
