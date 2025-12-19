# 006 – Kafka Connection Model

## Kafka Observatory

---

## 1. Purpose of This Document

This document describes how Kafka Observatory manages **Kafka connections**, including:

* Client creation and lifecycle
* Runtime configuration mapping
* Separation between Admin, Producer, and Consumer clients
* Connection reuse and cleanup strategies

The goal is to provide a **clear, safe, and predictable model** for interacting with Kafka.

---

## 2. Design Principles

The Kafka connection model follows these principles:

* Kafka clients are created programmatically at runtime
* Configuration is immutable during application lifetime
* Admin, Producer, and Consumer clients are isolated
* Kafka-specific logic is contained within adapters
* Resource cleanup is explicit and deterministic

---

## 3. KafkaConnectionManager

### 3.1 Responsibility

`KafkaConnectionManager` is the central component responsible for:

* Reading runtime Kafka configuration
* Building Kafka client properties
* Creating Kafka client instances
* Managing shared Kafka resources

It acts as a **single source of truth** for Kafka connectivity.

---

### 3.2 Scope and Lifetime

* One instance per application
* Initialized during application startup
* Lives for the entire lifecycle of the application

KafkaConnectionManager does not reconnect or reload configuration dynamically.

---

## 4. Kafka Client Types

Kafka Observatory uses three distinct Kafka client types:

| Client Type   | Purpose                         |
| ------------- | ------------------------------- |
| AdminClient   | Metadata and cluster operations |
| KafkaProducer | Message production              |
| KafkaConsumer | Message consumption             |

Each client type is configured and managed independently.

---

## 5. Client Creation Strategy

### 5.1 AdminClient

* Created once at startup
* Reused for all admin operations
* Closed gracefully on application shutdown

AdminClient is treated as a long-lived shared resource.

---

### 5.2 KafkaProducer

* Single shared instance
* Thread-safe usage
* Used for all produce requests

Producer is optimized for reuse and low overhead.

---

### 5.3 KafkaConsumer

* Created per consume session
* Not shared across sessions
* Runs in a dedicated background thread

This avoids concurrency issues and allows clean session isolation.

---

## 6. Consume Session Lifecycle

Each consume operation follows this lifecycle:

1. User initiates consumption via REST API
2. Core layer creates a consume session
3. KafkaConsumer is created for the session
4. Consumer starts polling in background
5. Messages are streamed via WebSocket
6. User stops consumption or session ends
7. Consumer is closed and resources released

Each session is isolated and independently managed.

---

## 7. Configuration Mapping

### 7.1 Source

Kafka client configuration is built from:

* Environment variables
* Application defaults

No configuration files are used.

---

### 7.2 Property Construction

Kafka properties are constructed once and reused:

* Common properties (bootstrap servers, security)
* Client-specific overrides (group.id, deserializers)

Configuration objects are immutable after creation.

---

## 8. Security Configuration Handling

KafkaConnectionManager handles:

* TLS configuration
* SASL authentication
* Secure credential injection

Security configuration is applied uniformly to all Kafka clients.

Sensitive data is never logged or exposed.

---

## 9. Error Handling Strategy

Kafka-related errors are handled as follows:

* Connection errors are detected early
* Client creation failures fail fast
* Runtime Kafka exceptions are captured per operation
* Errors are translated into application-level errors

The system avoids retry storms or hidden failures.

---

## 10. Shutdown and Cleanup

On application shutdown:

* AdminClient is closed
* Producer is flushed and closed
* Active consume sessions are stopped
* Consumer threads are terminated

This ensures graceful shutdown and prevents resource leaks.

---

## 11. Threading Model

* AdminClient: synchronous calls
* Producer: asynchronous send
* Consumer: dedicated thread per session

No Kafka client is accessed concurrently in unsafe ways.

---

## 12. MVP Constraints

The following constraints apply during MVP:

* Single Kafka cluster
* No dynamic reconnection
* No client pooling
* No multi-tenant isolation

These constraints are intentional and simplify the initial implementation.

---

## 13. Extensibility

The connection model allows future enhancements such as:

* Multi-cluster support
* Client pooling
* Dynamic reconnection
* OAuth / IAM authentication

These can be added without breaking existing abstractions.

---

## 14. Summary

Kafka Observatory uses a **centralized, explicit Kafka connection model** built around a dedicated `KafkaConnectionManager`.

This approach ensures:

* Predictable Kafka interactions
* Safe resource management
* Clear ownership of responsibilities
* A solid foundation for future growth
