## Kafka Observatory

---

## 1. Purpose of This Document

This document describes the **internal structure of the backend application**.

Its purpose is to:

* Define clear package boundaries
* Explain module responsibilities
* Prevent architectural drift
* Provide guidance for future development

The backend structure is aligned with the **modular monolith** and **hexagonal architecture** principles defined in earlier documents.

---

## 2. High-Level Package Layout

The backend application is organized into the following top-level packages:

```
com.kafka.observatory
│
├── core
├── ports
├── adapters
├── config
└── KafkaObservatoryApplication.kt
```

Each package has a clearly defined responsibility and dependency direction.

---

## 3. Dependency Rules

The following dependency rules apply:

* `core` must not depend on Spring, Kafka, or web frameworks
* `ports` must not depend on implementation details
* `adapters` may depend on external libraries
* `config` wires everything together
* Dependency direction always points **inward**

Violating these rules is considered an architectural error.

---

## 4. Core Package

### 4.1 Purpose

The `core` package contains the **application’s business logic and orchestration logic**.

Characteristics:

* Framework-agnostic
* Pure Kotlin code
* No annotations
* No infrastructure dependencies

---

### 4.2 Contents

```
core/
 ├── model
 ├── service
 └── session
```

---

### 4.3 Core Models

The `model` package contains core data structures used throughout the application.

Examples:

* Topic
* Partition
* Message
* ConsumeRequest

These models represent **Kafka concepts**, not domain-specific business entities.

---

### 4.4 Core Services

The `service` package contains application services that:

* Coordinate use cases
* Apply simple validation rules
* Orchestrate calls to ports

Examples:

* TopicService
* ConsumeService
* ProduceService

---

### 4.5 Session Management

The `session` package manages long-running operations such as message consumption.

Responsibilities:

* Tracking active consume sessions
* Managing lifecycle (start, stop)
* Handling resource cleanup

Session logic is kept in the core layer to avoid coupling to web or Kafka implementations.

---

## 5. Ports Package

### 5.1 Purpose

The `ports` package defines **interfaces** that describe what the application needs from external systems.

Ports are contracts, not implementations.

---

### 5.2 Port Types

```
ports/
 ├── kafka
 └── web
```

---

### 5.3 Kafka Ports

Kafka ports define operations such as:

* Listing topics
* Consuming messages
* Producing messages

Example:

```kotlin
interface TopicPort {
    fun listTopics(): List<Topic>
}
```

---

### 5.4 Web Ports

Web ports define how results are delivered to clients.

Examples:

* Message streaming
* Session notifications

These ports allow the core to remain unaware of HTTP or WebSocket details.

---

## 6. Adapters Package

### 6.1 Purpose

Adapters implement the ports using specific technologies.

They are the **only place** where external libraries and frameworks are used.

---

### 6.2 Adapter Types

```
adapters/
 ├── kafka
 │   ├── admin
 │   ├── consumer
 │   └── producer
 │
 └── web
     ├── rest
     └── websocket
```

---

### 6.3 Kafka Adapters

Kafka adapters are responsible for:

* Creating and managing Kafka clients
* Translating Kafka data structures to core models
* Handling Kafka-specific errors

Kafka adapters must not contain web logic.

---

### 6.4 Web Adapters

Web adapters expose application functionality via:

* REST endpoints
* WebSocket connections

Responsibilities:

* Request validation
* Response mapping
* Error translation

Web adapters must not contain Kafka client logic.

---

## 7. Config Package

### 7.1 Purpose

The `config` package wires together ports and adapters.

Responsibilities:

* Reading environment variables
* Building Kafka client configurations
* Registering Spring beans

---

### 7.2 Kafka Configuration

Kafka-related configuration includes:

* Bootstrap servers
* TLS configuration
* SASL configuration

This logic is centralized to avoid duplication and inconsistencies.

---

## 8. Application Entry Point

`KafkaObservatoryApplication.kt` is responsible for:

* Bootstrapping the Spring context
* Loading configuration
* Starting the web server

No business logic should reside in the application entry point.

---

## 9. Error Handling Strategy

* Infrastructure-specific errors are handled in adapters
* Core services handle application-level validation
* Web adapters translate errors into HTTP/WebSocket responses

This separation ensures clarity and maintainability.

---

## 10. Testing Strategy Alignment

The backend structure enables:

* Unit testing core services using mocked ports
* Integration testing Kafka adapters with test clusters
* API testing web adapters independently

Each layer can be tested in isolation.

---

## 11. Naming Conventions

* Packages use lowercase names
* Interfaces use the `Port` suffix
* Adapter implementations are suffixed with `Adapter`
* Services are suffixed with `Service`

Consistency is enforced to improve readability.

---

## 12. Evolution Guidelines

When adding new functionality:

1. Define or extend a port
2. Implement the port in an adapter
3. Invoke the port from a core service
4. Expose functionality via a web adapter if needed

Skipping steps or bypassing layers is discouraged.

---

## 13. Summary

The backend structure of Kafka Observatory is designed to be:

* Clear and predictable
* Easy to navigate
* Resistant to architectural erosion
* Well-suited for long-term maintenance

By enforcing strict boundaries and responsibilities, the backend remains robust while allowing future growth.
