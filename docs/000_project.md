## Kafka Observatory

### 1. Project Purpose

Kafka Observatory is a **lightweight, containerized Kafka UI** designed to connect to any Kafka cluster at runtime using a **single Docker command**.

The primary goal of the project is to provide a **fast, simple, and secure way to explore Kafka clusters** without requiring:
- Static configuration files
- Complex installations
- Multiple services or containers

Kafka Observatory is intended mainly for **internal usage**, development, debugging, and operational visibility.

---

### 2. Problem Statement

Existing Kafka UI tools often suffer from one or more of the following issues:

- Heavy setup and configuration requirements
- Dependency on static config files or YAMLs
- Complex multi-container deployments
- Limited runtime flexibility for TLS / authentication
- Overly complex UIs for simple inspection tasks

For engineers who need to **quickly connect to a Kafka cluster**, inspect topics, consume messages, or produce test data, these tools can introduce unnecessary friction.

---

### 3. Solution Overview

Kafka Observatory addresses these problems by providing:

- **Single-command startup** using `docker run`
- Runtime configuration via **environment variables**
- Support for **TLS and authentication**
- A clean and minimal **web-based UI**
- A **single deployable application** (monolith)

All Kafka communication is handled on the backend, while the frontend focuses purely on visualization and interaction.

---

### 4. Key Design Principles

The project is built around the following principles:

- **Simplicity over completeness**
- **Fast startup and ease of use**
- **Clear separation of concerns**
- **Production-oriented design**
- **Minimal infrastructure requirements**

Kafka Observatory intentionally avoids over-engineering and focuses on delivering practical value with a small, well-defined feature set.

---

### 5. Target Users

- Backend engineers
- Platform / infrastructure engineers
- Data engineers
- Developers working with Kafka-based systems

The tool is especially useful in environments where engineers frequently need to connect to different Kafka clusters with varying security configurations.

---

### 6. Architecture Summary

Kafka Observatory is implemented as a **modular monolith** using **hexagonal (ports & adapters) architecture**.

High-level architecture:

- **Backend**: Kotlin + Spring Boot
- **Frontend**: React + TypeScript
- **Deployment**: Single Docker image
- **Kafka Access**: Backend-only (no direct browser access)

The application consists of:
- Core domain logic (framework-agnostic)
- Kafka adapters (Admin, Consumer, Producer)
- Web adapters (REST API and WebSocket)
- A static frontend served by the backend

---

### 7. Configuration Model

All Kafka connection details are provided at runtime via environment variables. No configuration files are required.

Example:

```bash
docker run -p 8085:8080 \
  -e KAFKA_BROKERS="broker1:9093,broker2:9093" \
  -e KAFKA_TLS_ENABLED=true \
  -v /path/to/certs:/tls:ro \
  kafka-observatory:latest
```

---

### 8. MVP Scope (Initial Phase)

The first version of Kafka Observatory focuses on:

- Cluster connectivity validation
- Topic listing and inspection
- Message consumption (JSON / String / Raw)
- Message production (basic)
- Health and readiness checks

Advanced features such as schema registry support, consumer group management, and multi-cluster support are intentionally deferred to later phases.

---

### 9. Project Goals

- Provide a reliable Kafka exploration tool with minimal setup
- Demonstrate clean backend architecture and design principles
- Serve as a strong portfolio project showcasing Kafka, backend, and system design expertise
- Remain extensible without sacrificing simplicity

---

### 10. Non-Goals

Kafka Observatory is not intended to:

- Replace full-featured Kafka management platforms
- Provide administrative mutation capabilities (e.g., topic deletion, ACL management)
- Act as a monitoring or alerting system

### 11. Project Status

Current phase: Planning & Initialization

Next steps:

- Backend application bootstrap
- Healthcheck endpoint implementation
- Runtime configuration parsing
- Initial Docker setup

