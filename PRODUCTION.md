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

3.  **Distributed Tracing**:
    *   Integrated with OpenTelemetry.
    *   Service Name: `warehouse-monolith`
    *   Propagates trace context across requests.
    *   Implemented using `quarkus-opentelemetry`.

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

#### CI/CD Pipeline

A GitHub Actions workflow has been implemented in `.github/workflows/cd.yml`.

1.  **Build Stage**:
    *   Compiles the application using JDK 17.
    *   Runs all tests (unit and integration).
    *   Packages the application into a Quarkus app.
    *   Uploads the `target/quarkus-app/` as an artifact.

2.  **Docker Stage** (Main branch only):
    *   Downloads the build artifact.
    *   Builds a Docker image using `src/main/docker/Dockerfile.jvm`.
    *   Pushes the image to GitHub Container Registry (GHCR).
    *   Tags: `latest` and `SHA`.

3.  **Deployment Stage** (Main branch only):
    *   Currently a placeholder. To enable actual deployment, target environment details and credentials need to be provided.

#### Next Steps for Production

*   **Deployment Configuration**: Provide details for the target environment (e.g., Kubeconfig for K8s, API keys for Cloud providers) to complete the CD pipeline.
*   **Alerting**: Set up Prometheus/Grafana alerts based on the `/metrics` endpoint.
*   **Centralized Logging**: Stream JSON logs to an ELK or EFK stack.
