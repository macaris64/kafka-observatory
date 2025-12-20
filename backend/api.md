# Kafka Observatory API Documentation

This document outlines the available REST and WebSocket endpoints for the Kafka Observatory backend.

## Base URL
- **REST**: `http://localhost:8080/api`
- **WebSocket**: `ws://localhost:8080/ws`

---

## 1. System & Monitoring

### Health Check
Returns the current status of the application.

- **URL**: `/health`
- **Method**: `GET`
- **Success Response**:
  - **Code**: 200 OK
  - **Body**:
    ```json
    {
      "status": "UP",
      "app": "kafka-observatory",
      "version": "0.0.1"
    }
    ```

---

## 2. Kafka Cluster Information

### Get Cluster Details
Returns information about the connected Kafka cluster, including controller node, broker count, and cluster ID.

- **URL**: `/cluster`
- **Method**: `GET`
- **Success Response**:
  - **Code**: 200 OK
  - **Body**:
    ```json
    {
      "data": {
        "clusterId": "string",
        "controller": {
          "id": 1,
          "host": "localhost",
          "port": 9092
        },
        "brokers": [
          { "id": 1, "host": "localhost", "port": 9092 }
        ],
        "authorizedOperations": []
      }
    }
    ```

---

## 3. Topics

### List All Topics
Returns a list of all available topics in the cluster with partition counts and replication factors.

- **URL**: `/topics`
- **Method**: `GET`
- **Success Response**:
  - **Code**: 200 OK
  - **Body**:
    ```json
    {
      "data": [
        {
          "name": "my-topic",
          "partitionCount": 3,
          "replicationFactor": 1,
          "isInternal": false
        }
      ]
    }
    ```

---

## 4. Consume Sessions

### Create & Start Session
Initializes a new background Kafka consumer session.

- **URL**: `/consume-sessions`
- **Method**: `POST`
- **Request Body**:
  ```json
  {
    "topic": "my-topic",
    "groupId": "optional-group-id",
    "from": "EARLIEST", 
    "maxBufferSize": 500
  }
  ```
  - `from`: Supports `EARLIEST` or `LATEST` (Default: `LATEST`).
- **Success Response**:
  - **Code**: 200 OK
  - **Body**:
    ```json
    {
      "data": {
        "id": "uuid-session-id",
        "topic": "my-topic",
        "groupId": "...",
        "state": "RUNNING",
        "createdAt": "iso-timestamp"
      }
    }
    ```

### Get Session Status
Returns metadata and the current state of a session.

- **URL**: `/consume-sessions/{sessionId}`
- **Method**: `GET`
- **Success Response**:
  - **Code**: 200 OK
  - **Body**:
    ```json
    {
      "data": {
        "sessionId": "uuid",
        "topic": "...",
        "state": "RUNNING",
        "messageCount": 42,
        "lastConsumedAt": "iso-timestamp"
      }
    }
    ```

### Poll Messages (REST)
Retrieves a snapshot of recently consumed messages from the session's internal buffer.

- **URL**: `/consume-sessions/{sessionId}/messages`
- **Method**: `GET`
- **Query Params**:
  - `limit`: (Optional) Max messages to return (Default: 100).
- **Success Response**:
  - **Code**: 200 OK
  - **Body**:
    ```json
    {
      "data": [
        {
          "topic": "...",
          "partition": 0,
          "offset": 123,
          "timestamp": 1700000000,
          "key": "...",
          "value": "...",
          "headers": {}
        }
      ]
    }
    ```

### Pause Session
Temporarily stops polling from Kafka.

- **URL**: `/consume-sessions/{sessionId}/pause`
- **Method**: `POST`
- **Success Response**: `200 OK`

### Resume Session
Resumes polling for a paused session.

- **URL**: `/consume-sessions/{sessionId}/resume`
- **Method**: `POST`
- **Success Response**: `200 OK`

### Stop & Delete Session
Gracefully stops the consumer and clears session data.

- **URL**: `/consume-sessions/{sessionId}`
- **Method**: `DELETE`
- **Success Response**: `200 OK`

---

## 5. Real-time Streaming (WebSocket)

### Subscribe to Message Stream
Opens a WebSocket connection to receive real-time updates as messages are consumed.

- **URL**: `/ws/consume-sessions/{sessionId}`
- **Protocol**: `ws`
- **Flow**:
  1. Client establishes connection.
  2. Server verifies `sessionId`.
  3. Server pushes `ConsumedMessage` JSON objects to the client.
- **Backpressure**: If the client is slow, messages are dropped to prevent backend blocking.
- **Message Format**:
  ```json
  {
    "topic": "...",
    "partition": 0,
    "offset": 123,
    "timestamp": 1700000000,
    "key": "...",
    "value": "...",
    "headers": {}
  }
  ```
