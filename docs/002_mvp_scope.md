## Kafka Observatory

## 1. MVP Objective

The objective of the MVP (Minimum Viable Product) is to deliver a **usable and reliable Kafka exploration tool** that can be started with a single Docker command and allows users to:

- Verify Kafka connectivity
- Explore topics and partitions
- Consume messages from Kafka
- Produce test messages to Kafka

The MVP focuses on **core functionality only**, avoiding advanced features that increase complexity without immediate value.

---

## 2. MVP Definition of Done

The MVP is considered complete when:

- The application starts successfully using `docker run`
- Kafka connectivity can be verified via the UI
- Topics can be listed and inspected
- Messages can be consumed and displayed
- Messages can be produced to a selected topic
- The application remains stable during prolonged usage

---

## 3. In-Scope Features

### 3.1 Application & Infrastructure

- Single Spring Boot application
- Single Docker image
- Runtime configuration via environment variables
- Healthcheck endpoint
- No external dependencies required to run the app

---

### 3.2 Cluster Connectivity

- Read Kafka broker list from environment variables
- Establish Kafka AdminClient connection
- Retrieve cluster metadata:
  - Cluster ID
  - Broker list
  - Kafka version (if available)

Purpose:
- Validate that the application is correctly connected to the Kafka cluster

---

### 3.3 Topic Management (Read-Only)

- List all topics
- View topic details:
  - Number of partitions
  - Replication factor
  - Partition leaders and ISR

Constraints:
- Topics are **read-only**
- No topic creation, deletion, or configuration changes

---

### 3.4 Message Consumption

- Consume messages from a selected topic
- Consumption options:
  - From earliest offset
  - From latest offset
  - From a specific offset
- Consume from:
  - All partitions
  - A specific partition

Message details displayed:
- Key
- Headers
- Value
- Partition
- Offset
- Timestamp

---

### 3.5 Message Decoding (MVP Level)

Supported formats:
- JSON (auto-detected)
- UTF-8 String
- Raw bytes (hex view)

Constraints:
- No schema registry integration
- No Avro or Protobuf decoding in MVP

---

### 3.6 Message Production

- Produce messages to a selected topic
- Supported input:
  - Key (string)
  - Headers (key-value pairs)
  - Value (JSON or String)

Constraints:
- No partition selection
- No advanced producer configuration (acks, retries, idempotence)
- Fire-and-forget semantics are acceptable for MVP

---

### 3.7 User Interface

Pages included in MVP:
- Topics list page
- Topic consumption page
- Message production page

UI characteristics:
- Simple and responsive
- No onboarding or wizard flows
- Focused on functionality over aesthetics

---

## 4. Out-of-Scope Features

The following features are explicitly excluded from the MVP:

- Schema Registry integration
- Avro message decoding
- Protobuf message decoding
- Consumer group management
- Lag calculation and visualization
- Topic configuration editing
- Topic creation or deletion
- ACL management
- Authentication and authorization UI
- Multi-cluster support
- Monitoring, metrics, or alerting

These features may be considered in future phases.

---

## 5. Non-Functional Requirements

- Fast startup time
- Predictable resource usage
- Graceful handling of Kafka connection failures
- Clear error messages in the UI
- No data persistence within the application

---

## 6. MVP Limitations

- Only one Kafka cluster can be connected at a time
- Kafka configuration changes require container restart
- Message consumption is intended for debugging and inspection, not high-throughput processing

These limitations are acceptable and intentional for the MVP.

---

## 7. MVP Success Criteria

The MVP is successful if:

- An engineer can connect to a Kafka cluster within minutes
- No configuration files are required
- Basic Kafka inspection tasks can be completed without additional tools
- The tool can be reliably used during development or debugging sessions

---

## 8. Future Phases (Beyond MVP)

Features deferred to later phases include:

- Consumer group visibility and lag metrics
- Avro and Protobuf decoding
- Schema Registry integration
- Multi-cluster support
- Role-based access control
- Advanced producer and consumer configuration

These features are intentionally excluded to keep the MVP focused and maintainable.

---

## 9. Summary

The MVP scope of Kafka Observatory is intentionally narrow and practical.

By limiting the feature set to essential Kafka exploration capabilities, the MVP:
- Reduces development risk
- Enables faster delivery
- Establishes a solid foundation for future enhancements

The scope defined in this document serves as a guardrail against unnecessary complexity and scope creep.
