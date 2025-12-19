# 008 – Consume Session Flow

## Kafka Observatory

---

## 1. Purpose of This Document

This document describes the **message consumption flow** in Kafka Observatory.

It focuses on:

* How consume sessions are created and managed
* How Kafka consumers interact with the backend
* How messages are streamed to the frontend
* How session lifecycle and cleanup are handled

The consume session model is a core part of the application and must remain predictable and safe.

---

## 2. Consume Session Concept

A **consume session** represents a single, isolated message consumption operation initiated by a user.

Each session:

* Is associated with one topic
* May target one or all partitions
* Has a defined starting offset
* Owns its own KafkaConsumer instance

Sessions are independent and do not share Kafka consumers.

---

## 3. High-Level Flow Overview

```
User (UI)
  ↓
POST /api/consume/start
  ↓
Core: ConsumeService
  ↓
Create ConsumeSession
  ↓
KafkaConsumerAdapter
  ↓
Kafka Poll Loop (Thread)
  ↓
MessageDecoder
  ↓
WebSocket Stream
  ↓
Frontend UI
```

---

## 4. Session Creation Flow

1. User submits a consume request via the UI
2. Frontend sends a request to `POST /api/consume/start`
3. Web adapter validates the request
4. Core service creates a new consume session
5. A unique `sessionId` is generated
6. KafkaConsumerAdapter creates a KafkaConsumer instance
7. The consumer thread is started

The session is now active.

---

## 5. Kafka Consumer Initialization

For each consume session:

* A new KafkaConsumer is created
* Deserializers are configured for byte-level access
* The consumer subscribes or assigns partitions
* Offset positioning is applied (earliest, latest, or specific)

Consumer configuration is isolated per session.

---

## 6. Polling Loop

Each consume session runs a dedicated polling loop:

* Executed in a background thread
* Uses a controlled polling interval
* Continues until the session is stopped

Pseudo-flow:

```
while (session.isActive) {
  records = consumer.poll(timeout)
  for (record in records) {
    process(record)
  }
}
```

---

## 7. Message Processing Pipeline

For each consumed record:

1. Raw Kafka record is received
2. Metadata is extracted (topic, partition, offset, timestamp)
3. Headers are parsed
4. Value is passed to the message decoder
5. Decoded message is prepared for streaming

The processing pipeline is synchronous per record.

---

## 8. Message Decoding

The decoder determines how the message value is interpreted.

MVP-supported decoders:

* JSON
* UTF-8 String
* Raw bytes (hex)

Decoder selection is automatic based on message content.

---

## 9. WebSocket Streaming

Decoded messages are sent to the frontend via WebSocket.

Characteristics:

* One WebSocket connection per UI client
* Messages include `sessionId` for correlation
* Messages are sent in near real-time

The WebSocket layer does not buffer messages.

---

## 10. Session Termination Flow

A consume session can be terminated by:

* User action (`POST /api/consume/stop`)
* WebSocket disconnect
* Application shutdown
* Internal error

Termination steps:

1. Session marked as inactive
2. Poll loop exits
3. KafkaConsumer is closed
4. Resources are released

---

## 11. Error Handling During Consumption

Errors during consumption are handled as follows:

* Kafka exceptions are caught per session
* Errors are logged with session context
* Session is stopped gracefully
* Frontend is notified via WebSocket

One session failure does not impact other sessions.

---

## 12. Resource Management

Resource safety guarantees:

* One KafkaConsumer per session
* No shared mutable consumer state
* Explicit thread termination
* Guaranteed consumer close

This prevents resource leaks and unstable behavior.

---

## 13. MVP Constraints

During MVP:

* Only one active consume session per user
* No session persistence
* No resume after restart
* No backpressure handling

These constraints simplify the initial implementation.

---

## 14. Future Enhancements

Potential future improvements include:

* Multiple concurrent sessions per user
* Backpressure and buffering
* Pause and resume functionality
* Message filtering
* Session persistence

These features can be added without breaking the core flow.

---

## 15. Summary

Kafka Observatory’s consume session flow is designed to be:

* Safe and isolated
* Easy to reason about
* Resource-efficient
* Well-aligned with MVP goals

By treating each consume operation as a first-class session, the system avoids complexity while remaining extensible.
