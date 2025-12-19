# 010 – Development & Run Modes

## Kafka Observatory

---

## 1. Purpose of This Document

This document explains **how Kafka Observatory can be run in different modes** depending on the user’s intent:

* End users who only want to *use* the application
* Developers who want to *contribute* or *extend* the project

It describes how the application can be executed:

* Using Docker
* Locally (backend only, frontend only, or both)
* With different runtime profiles (`local`, `dev`, `prod`)

The goal is to make the project **easy to use** and **easy to develop** without compromising simplicity.

---

## 2. Is This Setup Possible?

**Yes, this setup is fully possible and recommended.**

Kafka Observatory is explicitly designed to:

* Run as a single Docker container for users
* Run as separate frontend and backend services for developers
* Support multiple runtime profiles

This approach is common in production-grade projects and aligns well with the project’s architecture.

---

## 3. User vs Developer Modes

### 3.1 User Mode (Docker Only)

Target audience:

* Engineers who want to inspect Kafka quickly
* Users who do not want to modify the code

Characteristics:

* Application runs via Docker
* Single command startup
* Frontend and backend bundled together
* Configuration via environment variables

Example:

```bash
docker run -p 8085:8080 \
  -e SPRING_PROFILES_ACTIVE=prod \
  -e KAFKA_BROKERS="broker1:9092" \
  kafka-observatory:latest
```

---

### 3.2 Developer Mode (Local)

Target audience:

* Contributors
* Maintainers
* Developers extending functionality

Characteristics:

* Frontend and backend can be run independently
* Hot reload for frontend
* Fast iteration cycle

---

## 4. Backend Execution Modes

### 4.1 Backend – Local Mode

Run backend directly on the host machine.

```bash
cd backend
./gradlew bootRun
```

Defaults:

* Runs on `http://localhost:8080`
* Uses `local` Spring profile

---

### 4.2 Backend – Docker Mode

Backend can also be run via Docker.

```bash
docker run -p 8085:8080 \
  -e SPRING_PROFILES_ACTIVE=local \
  kafka-observatory:latest
```

This mirrors production behavior while still using local configuration.

---

## 5. Frontend Execution Modes

### 5.1 Frontend – Local Development

Run frontend independently with hot reload.

```bash
cd frontend
npm install
npm run dev
```

Defaults:

* Runs on `http://localhost:5173`
* Proxies API calls to backend

---

### 5.2 Frontend – Production Build

```bash
npm run build
```

The output is placed in the `dist/` directory and served by the backend in Docker or production mode.

---

## 6. Running Frontend and Backend Together (Local)

Developers can run both services locally:

* Backend on `localhost:8080`
* Frontend on `localhost:5173`

Communication:

* Frontend calls backend APIs directly
* CORS enabled only in `local` profile

This setup provides the fastest development feedback loop.

---

## 7. Spring Profiles

Kafka Observatory uses Spring profiles to control runtime behavior.

Supported profiles:

| Profile | Purpose                      |
| ------- | ---------------------------- |
| local   | Local development            |
| dev     | Shared development / staging |
| prod    | Production                   |

Profile is selected via:

```bash
SPRING_PROFILES_ACTIVE=local
```

---

## 8. Profile-Specific Behavior

### 8.1 local

* CORS enabled
* Verbose logging
* No frontend bundling required

---

### 8.2 dev

* Similar to production
* Debug-level logging optional
* Frontend may be bundled

---

### 8.3 prod

* Frontend served as static assets
* Strict CORS
* Optimized logging

---

## 9. Docker and Profiles

Docker images support all profiles.

Example:

```bash
docker run -p 8085:8080 \
  -e SPRING_PROFILES_ACTIVE=dev \
  kafka-observatory:latest
```

This allows the same image to be reused across environments.

---

## 10. Development Workflow Summary

### For Users

* Use Docker only
* Single command startup
* No local dependencies

---

### For Developers

* Run backend locally or in Docker
* Run frontend locally with hot reload
* Use `local` Spring profile

---

## 11. Why This Approach Works Well

This setup:

* Keeps user experience simple
* Enables fast development
* Avoids maintaining multiple deployment artifacts
* Aligns with modern full-stack development practices

---

## 12. Summary

Kafka Observatory supports multiple execution models without increasing complexity.

By clearly separating **usage** and **development** workflows and leveraging Spring profiles, the project remains:

* Easy to run
* Easy to develop
* Easy to operate

This flexibility is a deliberate and important design choice.
