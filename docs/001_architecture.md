## Kafka Observatory

---

## 1. Architectural Goals

The architecture of Kafka Observatory is designed to achieve the following goals:

- Enable **single-container deployment**
- Support **runtime Kafka configuration**
- Keep Kafka-specific complexity isolated
- Allow fast iteration without architectural lock-in
- Remain understandable and maintainable as a solo or small-team project

The architecture intentionally prioritizes **clarity and pragmatism** over maximal abstraction.

---

## 2. Architectural Style

Kafka Observatory follows a **Modular Monolith** architecture combined with **Hexagonal Architecture (Ports & Adapters)** principles.

### Key characteristics:

- **Single deployable application**
- Clear separation between:
  - Core application logic
  - Infrastructure concerns (Kafka, Web)
- No direct dependency from core logic to frameworks or external systems

This approach provides many of the benefits of clean architecture without the overhead of microservices.

---

---

## 3. Why Not Event-Driven Architecture?

Kafka Observatory is a Kafka *consumer and explorer*, not a domain that produces or reacts to business events.

- No complex asynchronous business workflows
- No eventual consistency requirements
- No domain events of its own

Using an event-driven architecture internally would introduce unnecessary complexity and provide little value for the problem being solved.

---

## 4. Why Not Full Domain-Driven Design (DDD)?

The project does not contain a rich business domain with complex rules, aggregates, or invariants.

The primary concepts (topics, partitions, messages, offsets) are:
- Technical Kafka abstractions
- Already well-defined externally
- Lacking domain-specific behavior

As a result, a full DDD approach would be artificial and heavy-handed.

---

## 5. Chosen Approach: Modular Monolith + Hexagonal Architecture

### Core Principles Applied

- **Ports define behavior**
- **Adapters implement infrastructure**
- Core logic does not depend on:
  - Spring
  - Kafka clients
  - Web frameworks

This allows:
- Easier testing
- Clear ownership of responsibilities
- Safer refactoring

---

## 6. High-Level Architecture Diagram (Logical)

+-------------------+
| Frontend (UI) |
| React + TypeScript|
+---------+---------+
|
| HTTP / WebSocket
v
+---------+---------+
| Web Adapters |
| (REST / WS APIs) |
+---------+---------+
|
| Ports
v
+---------+---------+
| Core Layer |
| Services & Models|
+---------+---------+
|
| Ports
v
+---------+---------+
| Kafka Adapters |
| (Admin / Consume /|
| Produce) |
+-------------------+


---

## 7. Module Responsibilities

### 7.1 Core Layer

The core layer contains:

- Application services
- Core models
- Port interfaces

Characteristics:
- No framework annotations
- No Kafka or Spring dependencies
- Pure Kotlin code

Example responsibilities:
- Defining what it means to list topics
- Managing consume session lifecycle
- Coordinating message decoding

---

---

### 7.2 Ports

Ports define **what the application needs**, not how it is done.

Examples:
- Listing Kafka topics
- Consuming messages
- Producing messages

Ports are expressed as Kotlin interfaces and live in the core layer.

---

### 7.3 Kafka Adapters

Kafka adapters implement the ports using Kafka clients.

Responsibilities:
- AdminClient interactions
- Consumer polling and offset management
- Producer message publishing
- Kafka-specific configuration handling

Kafka adapters are the **only place** where Kafka client libraries are used.

---

### 7.4 Web Adapters

Web adapters expose the application to the outside world.

Responsibilities:
- REST APIs for querying metadata
- WebSocket endpoints for live consumption
- Request/response mapping
- Input validation

Web adapters do not contain Kafka logic directly.

---

## 8. Frontend & Backend Interaction

- The frontend never communicates with Kafka directly
- All Kafka access is performed by the backend
- Communication happens via:
  - REST APIs (metadata, produce)
  - WebSocket (live consume)

The frontend is served as static content by the backend in production.

---

## 9. Deployment Architecture

Kafka Observatory is deployed as a **single Docker container**.

Characteristics:
- One process
- One exposed port
- Optional volume mount for TLS certificates
- Configuration via environment variables only

This design simplifies usage and avoids operational overhead.

---

## 10. Runtime Configuration Flow

1. Docker container starts
2. Environment variables are read
3. Kafka client configuration is built dynamically
4. Kafka connections are established on demand
5. UI interacts with the backend APIs

No static configuration files are required.

---

## 11. Testing Strategy (Architectural Impact)

The chosen architecture enables:

- Unit testing core services with mocked ports
- Integration testing Kafka adapters separately
- API testing web adapters independently

Kafka is not required for testing core application logic.

---

## 12. Architectural Constraints

The architecture intentionally enforces the following constraints:

- Kafka clients must not be used outside Kafka adapters
- Web layer must not depend directly on Kafka clients
- Core layer must remain framework-agnostic
- Only one Kafka cluster is supported in the MVP

---

## 13. Evolution Strategy

The architecture allows future extensions without major refactoring:

- Additional decoders (Avro / Protobuf)
- Consumer group visibility
- Multi-cluster support
- Authentication & authorization layers

These can be added by introducing new adapters or extending existing ports.

---

## 14. Summary

Kafka Observatory’s architecture is intentionally simple, modular, and pragmatic.

By combining a **modular monolith** with **hexagonal architecture**, the project achieves:
- Clear separation of concerns
- Low operational overhead
- Strong extensibility
- High maintainability

This architecture is well-suited for internal tools that must be reliable, flexible, and easy to deploy.
