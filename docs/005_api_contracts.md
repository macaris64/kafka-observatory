# 005 – API Contracts

## Kafka Observatory

---

## 1. Purpose of This Document

This document defines the **external API contracts** exposed by the Kafka Observatory backend.

Its purpose is to:

* Clearly document REST and WebSocket endpoints
* Define request and response structures
* Establish a stable contract between frontend and backend
* Prevent breaking changes during MVP development

All APIs described here are scoped to the **MVP phase** unless explicitly stated otherwise.

---

## 2. API Design Principles

The API design follows these principles:

* Clear and predictable URLs
* JSON as the primary data format
* Stateless REST endpoints
* WebSocket for streaming use cases
* Explicit separation between metadata and streaming APIs

All endpoints are prefixed with `/api` to avoid conflicts with frontend routing.

---

## 3. Common Conventions

### 3.1 Base URL

```
/api
```

---

### 3.2 Response Format

All REST endpoints return JSON responses.

Standard success response example:

```json
{
  "data": { ... }
}
```

Standard error response example:

```json
{
  "error": {
    "code": "INVALID_REQUEST",
    "message": "Description of the error"
  }
}
```

---

### 3.3 Error Codes (MVP)

* `INVALID_REQUEST`
* `KAFKA_CONNECTION_FAILED`
* `KAFKA_OPERATION_FAILED`
* `RESOURCE_NOT_FOUND`

---

## 4. Health & System APIs

### 4.1 Health Check

**Endpoint**

```
GET /api/health
```

**Response**

```json
{
  "status": "UP",
  "app": "kafka-observatory",
  "version": "0.0.1"
}
```

Purpose:

* Verify that the application is running
* Used for container and system health checks

---

## 5. Cluster APIs

### 5.1 Get Cluster Information

**Endpoint**

```
GET /api/cluster
```

**Response**

```json
{
  "data": {
    "clusterId": "abc123",
    "brokers": [
      {
        "id": 1,
        "host": "broker1",
        "port": 9092
      }
    ]
  }
}
```

Purpose:

* Validate Kafka connectivity
* Display basic cluster metadata

---

## 6. Topic APIs

### 6.1 List Topics

**Endpoint**

```
GET /api/topics
```

**Response**

```json
{
  "data": [
    {
      "name": "orders",
      "partitionCount": 3,
      "replicationFactor": 2
    }
  ]
}
```

---

### 6.2 Get Topic Details

**Endpoint**

```
GET /api/topics/{topicName}
```

**Response**

```json
{
  "data": {
    "name": "orders",
    "partitions": [
      {
        "partition": 0,
        "leader": 1,
        "isr": [1, 2]
      }
    ]
  }
}
```

Purpose:

* Inspect topic structure and partition details

---

## 7. Message Consumption APIs

### 7.1 Start Consumption (REST)

**Endpoint**

```
POST /api/consume/start
```

**Request**

```json
{
  "topic": "orders",
  "partition": null,
  "offset": "latest"
}
```

Notes:

* `partition` may be null to consume from all partitions
* `offset` values: `earliest`, `latest`, or numeric offset

**Response**

```json
{
  "data": {
    "sessionId": "session-123"
  }
}
```

---

### 7.2 Stop Consumption

**Endpoint**

```
POST /api/consume/stop
```

**Request**

```json
{
  "sessionId": "session-123"
}
```

**Response**

```json
{
  "data": {
    "status": "stopped"
  }
}
```

---

## 8. Message Streaming (WebSocket)

### 8.1 Consume WebSocket

**Endpoint**

```
/ws/consume
```

**Message Format**

```json
{
  "sessionId": "session-123",
  "topic": "orders",
  "partition": 0,
  "offset": 102,
  "timestamp": 1710000000,
  "key": "order-1",
  "headers": {
    "source": "api"
  },
  "value": {
    "raw": "{...}",
    "decoded": { ... },
    "type": "json"
  }
}
```

Purpose:

* Stream consumed messages to the frontend in real time

---

## 9. Message Production APIs

### 9.1 Produce Message

**Endpoint**

```
POST /api/produce
```

**Request**

```json
{
  "topic": "orders",
  "key": "order-1",
  "headers": {
    "source": "ui"
  },
  "value": {
    "type": "json",
    "payload": { "id": 1, "status": "CREATED" }
  }
}
```

**Response**

```json
{
  "data": {
    "status": "sent"
  }
}
```

Purpose:

* Produce test messages to Kafka topics

---

## 10. API Stability and Versioning

* APIs are unversioned during MVP
* Breaking changes are allowed during early development
* Versioning will be introduced once the API stabilizes

---

## 11. Security Considerations

* APIs are intended for internal use
* No authentication or authorization in MVP
* Kafka security is handled at the client level
* APIs do not expose credentials or sensitive data

---

## 12. Summary

The API contracts defined in this document establish a clear and minimal interface between the frontend and backend.

They are designed to:

* Support MVP functionality
* Enable rapid frontend development
* Remain extensible for future enhancements

This document serves as the authoritative reference for backend API behavior during the MVP phase.
