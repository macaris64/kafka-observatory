## Kafka Observatory

---

## 1. Configuration Philosophy

Kafka Observatory is designed to be configured **entirely at runtime**.

Key principles:

- No configuration files
- No YAML or JSON configs
- No rebuilds for configuration changes
- Configuration is provided via **environment variables only**

This approach ensures:
- Fast startup
- Easy experimentation
- Simple container usage
- Predictable behavior across environments

---

## 2. Configuration Lifecycle

The runtime configuration lifecycle is as follows:

1. Docker container starts
2. Environment variables are read on startup
3. Kafka client configuration is built dynamically
4. Kafka connections are established on demand
5. Configuration remains immutable until container restart

Any configuration change requires restarting the container.

---

## 3. Required Environment Variables

### 3.1 Kafka Brokers

```bash
KAFKA_BROKERS=broker1:9092,broker2:9092
```

Description:

- Comma-separated list of Kafka bootstrap servers
- Required for application startup

---

## 4. Optional Security Configuration

- Kafka Observatory supports secure Kafka clusters via TLS and SASL.
- All security configuration is optional and enabled only when explicitly provided.

---

## 5. TLS Configuration

### 5.1 Enable TLS

```bash
KAFKA_TLS_ENABLED=true
```

When enabled, the application configures Kafka clients to use SSL.

---

### 5.2 TLS Certificate Mounting

Certificates are provided via a mounted volume.

```bash
-v /path/to/certs:/tls:ro
```

---

The application expects certificates to be available inside the container under /tls.

### 5.3 TLS Environment Variables

```bash
KAFKA_TLS_CA_FILE=/tls/ca.pem
KAFKA_TLS_CERT_FILE=/tls/client.pem
KAFKA_TLS_KEY_FILE=/tls/client.key
```

Description:

- KAFKA_TLS_CA_FILE: Certificate Authority file
- KAFKA_TLS_CERT_FILE: Client certificate
- KAFKA_TLS_KEY_FILE: Client private key

All paths are container-internal paths.

---

## 6. SASL Authentication (Optional)

Kafka Observatory supports SASL-based authentication mechanisms.

### 6.1 Enable SASL

```bash
KAFKA_SASL_ENABLED=true
```

---

### 6.2 SASL Mechanism

```bash
KAFKA_SASL_MECHANISM=SCRAM-SHA-512
```

Supported mechanisms (MVP):

- SCRAM-SHA-256
- SCRAM-SHA-512


---

### 6.3 SASL Credentials

```bash
KAFKA_SASL_USERNAME=username
KAFKA_SASL_PASSWORD=password
```

---

## 7. Combined TLS + SASL Example

```bash
docker run -p 8085:8080 \
  -e KAFKA_BROKERS="broker1:9093,broker2:9093" \
  -e KAFKA_TLS_ENABLED=true \
  -e KAFKA_SASL_ENABLED=true \
  -e KAFKA_SASL_MECHANISM=SCRAM-SHA-512 \
  -e KAFKA_SASL_USERNAME=user \
  -e KAFKA_SASL_PASSWORD=secret \
  -v /path/to/certs:/tls:ro \
  kafka-observatory:latest
```

---

## 8. Internal Configuration Mapping

At application startup:

- Environment variables are mapped into an internal configuration model
- Kafka client properties are built programmatically
- Configuration is shared across:
    - AdminClient
    - Consumer
    - Producer

This ensures consistency between Kafka operations.

---

## Error Handling Strategy

If required configuration is missing or invalid:

- Application startup fails fast
- Clear error messages are logged
- The container exits with a non-zero status

Examples:

- Missing KAFKA_BROKERS
- TLS enabled but certificate paths are invalid
- SASL enabled without credentials

---

## 10. Security Considerations

- Secrets are never logged
- No credentials are exposed via APIs
- Certificates are read-only inside the container
- Configuration is kept in memory only

Kafka Observatory does not store or persist any sensitive data.

---

## 11. Configuration Limitations

The following limitations apply in the MVP:

- Only one Kafka cluster can be configured per container
- Configuration cannot be changed without restart
- No dynamic reconfiguration via UI or API

These constraints are intentional and aligned with the MVP goals.

---

## 12. Extensibility

The runtime configuration model allows future extensions, including:

- OAuth / IAM authentication
- Multiple Kafka cluster profiles
- UI-based configuration validation (read-only)

Such features can be added without breaking existing behavior.

---

## 13. Summary

Kafka Observatory uses a runtime-only configuration model to maximize usability and simplicity.

By relying exclusively on environment variables and container runtime configuration, the application achieves:

- Fast startup
- Minimal setup effort
- Predictable behavior
- Strong security guarantees

This configuration approach is a core part of the project's design philosophy.

