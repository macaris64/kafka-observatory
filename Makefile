.PHONY: build test test-local test-local-coverage test-local-coverage-verify run-local down-local build-local-fe build-local-be build-local kill-8080 kill-5173

# --- Build Targets ---

# Build everything (FE + BE)
build-local: build-local-be build-local-fe
	@echo "✅ Frontend + Backend build completed"

# Build the backend application
build-local-be:
	cd backend && ./gradlew spotlessApply spotlessKotlinGradle compileKotlin compileTestKotlin build

# Build the frontend application
build-local-fe:
	cd frontend && npm install && npm run lint:fix && npm run build

# --- Test Targets ---

# Run all tests locally (Backend + Frontend)
test-local:
	@echo "Running backend tests..."
	cd backend && ./gradlew test
	@echo "Running frontend tests..."
	cd frontend && npm test

# Run all tests with coverage reports
test-local-coverage:
	@echo "Running backend tests with coverage..."
	cd backend && ./gradlew test jacocoTestReport
	@echo "Running frontend tests with coverage..."
	cd frontend && npm run test:coverage

# Verify coverage for both FE and BE (fails if coverage < 70%)
test-local-coverage-verify:
	@echo "Verifying backend coverage (threshold: 70%)..."
	cd backend && ./gradlew test jacocoTestReport jacocoTestCoverageVerification
	@echo "Verifying frontend coverage (threshold: 70%)..."
	cd frontend && npm run test:coverage

# --- Execution Targets ---

# Run both FE and BE locally
run-local:
	@echo "Starting local infrastructure (Kafka/Docker)..."
	cd localdev && docker-compose up -d
	@echo "Waiting for Kafka to be ready..."
	@sleep 5
	@echo "Starting services (Backend on 8080, Frontend on 5173)..."
	@# Using background processes for local run. 
	@# We use 'trap' to handle cleanup if run-local is interrupted (Ctrl+C).
	(cd backend && SPRING_PROFILES_ACTIVE=local ./gradlew bootRun) & \
	(cd frontend && npm run dev) & \
	wait

# Kill processes on specific ports gracefully
kill-8080:
	@echo "Checking for process on port 8080..."
	@PIDS=$$(lsof -ti :8080); \
	if [ -n "$$PIDS" ]; then \
		echo "Stopping backend processes ($$PIDS)..."; \
		kill -15 $$PIDS 2>/dev/null || kill -9 $$PIDS 2>/dev/null; \
	fi
	@echo "Port 8080 is free."

kill-5173:
	@echo "Checking for process on port 5173..."
	@PIDS=$$(lsof -ti :5173); \
	if [ -n "$$PIDS" ]; then \
		echo "Stopping frontend processes ($$PIDS)..."; \
		kill -15 $$PIDS 2>/dev/null || kill -9 $$PIDS 2>/dev/null; \
	fi
	@echo "Port 5173 is free."

# Stop everything
down-local:
	@$(MAKE) kill-8080
	@$(MAKE) kill-5173
	@echo "Stopping local infrastructure (Kafka/Docker)..."
	cd localdev && docker-compose down
	@echo "Local environment stopped."
