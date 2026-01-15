### Production Readiness Guide

This document outlines the observability and scalability features implemented to make the application production-ready.

#### Observability

1.  **Health Checks**:
    *   Endpoint: `/health`
    *   Includes Liveness, Readiness, and Group health checks.
    *   Provides status of the application and its dependencies (e.g., Database).
    *   Implemented using `quarkus-smallrye-health`.

2.  **Metrics**:
    *   Endpoint: `/metrics`
    *   Format: Prometheus
    *   Includes JVM metrics, HTTP request metrics, and custom application metrics.
    *   Implemented using `quarkus-micrometer-registry-prometheus`.

3.  **Structured Logging**:
    *   Configured for JSON logging in production environment (`%prod` profile).
    *   Standardized console log format for non-production environments.
    *   Level set to `INFO` by default.

#### Scalability

1.  **Database Connection Pooling**:
    *   Configured Agroal connection pool.
    *   `max-size` set to 20 to handle concurrent requests.
    *   `min-size` set to 5 to maintain a baseline of ready connections.

2.  **Containerization Support**:
    *   The application is ready to be built as a Docker image (using provided Dockerfiles in `src/main/docker`).
    *   Quarkus native build support for minimal footprint and fast startup.

3.  **Resource Management**:
    *   Hibernate generation set to `none` in production to prevent accidental schema changes.
    *   SQL logging disabled in production for performance.

#### Next Steps for Production

*   **Distributed Tracing**: Integrate with OpenTelemetry for end-to-end request tracing.
*   **Alerting**: Set up Prometheus/Grafana alerts based on the `/metrics` endpoint.
*   **Centralized Logging**: Stream JSON logs to an ELK or EFK stack.
