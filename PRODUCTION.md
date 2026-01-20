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

#### Deployment to Google Cloud Platform (GCP)

The recommended way to deploy this application to GCP is using **Google Cloud Run** for the application layer and **Cloud SQL** for the database.

##### 1. Infrastructure Setup

*   **Cloud SQL (PostgreSQL)**: Create a PostgreSQL instance. Ensure it's in the same region as your Cloud Run service for better performance.
    *   **Connection Name**: Note down the "Connection name" from the Cloud SQL instance overview (e.g., `project:region:instance`).
*   **Artifact Registry**: Create a Docker repository in Artifact Registry to store your images.
*   **Service Account**: Create a Service Account for GitHub Actions with the following roles:
    *   `roles/run.admin` (Cloud Run Admin)
    *   `roles/iam.serviceAccountUser` (Service Account User)
    *   `roles/artifactregistry.writer` (Artifact Registry Writer)
    *   `roles/cloudsql.client` (Cloud SQL Client) - **Mandatory** for connecting to Cloud SQL.

##### 2. GitHub Actions Configuration

To enable the automated CD pipeline to GCP, set up the following:

**GitHub Actions Secrets:**

Go to **Settings > Secrets and variables > Actions** in your GitHub repository and add the following **Repository secrets**:

*   `GCP_SA_KEY`: The JSON key of your Service Account (copy the entire contents of the `.json` file).
*   `DB_URL`: The JDBC URL for your Cloud SQL instance.
    *   **Direct IP (Private or Public)**: `jdbc:postgresql://<DB_IP>:5432/<DB_NAME>`
    *   **Cloud SQL Auth Proxy (Unix Socket)**: `jdbc:postgresql:///<DB_NAME>?host=/cloudsql/<INSTANCE_CONNECTION_NAME>`
        *   *Note: Using Unix sockets is the recommended way for Cloud Run.*
    *   **With Google Managed Internal CA**: If you have "Google managed internal certificate authority" enabled, the Cloud SQL Auth Proxy (used by Cloud Run) handles this automatically. You **do not** need to add SSL parameters to your JDBC URL when using the Unix socket approach.
    *   **Direct SSL Connection (If not using Proxy)**: If you are connecting directly via IP and want to verify the CA:
        *   `jdbc:postgresql://<DB_IP>:5432/<DB_NAME>?ssl=true&sslmode=verify-ca&sslrootcert=/path/to/server-ca.pem`
*   `DB_USER`: Database username (e.g., `postgres`).
*   `DB_PASSWORD`: Database password.

**GitHub Actions Variables:**

In the same section, go to the **Variables** tab and add the following **Repository variables**:
*   `GCP_PROJECT_ID`: Your GCP Project ID.
*   `GCP_REGION`: Target region (e.g., `europe-west1`).
*   `GCP_AR_REPO`: The name of your Artifact Registry repository.
*   `CLOUD_SQL_INSTANCE`: The full "Connection name" of your Cloud SQL instance (e.g., `my-project:europe-west1:my-instance`).

##### 3. Manual Deployment (Optional)

If you prefer to deploy manually:

```bash
# Build the application
./mvnw package

# Build and push the image to Artifact Registry
gcloud builds submit --tag gcr.io/[PROJECT_ID]/warehouse-monolith .

# Deploy to Cloud Run
gcloud run deploy warehouse-monolith \
  --image gcr.io/[PROJECT_ID]/warehouse-monolith \
  --platform managed \
  --region [REGION] \
  --set-env-vars="QUARKUS_DATASOURCE_JDBC_URL=jdbc:postgresql://[DB_IP]:5432/[DB_NAME],QUARKUS_DATASOURCE_USERNAME=[USER],QUARKUS_DATASOURCE_PASSWORD=[PASSWORD]" \
  --allow-unauthenticated
```

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
    *   Deploys to Google Cloud Run.
    *   Requires GCP credentials and configuration (see Deployment to GCP section).

#### Next Steps for Production

*   **Infrastructure Provisioning**: Use Terraform or similar Iac tool to provision GCP resources.
*   **Alerting**: Set up Prometheus/Grafana alerts based on the `/metrics` endpoint.
*   **Centralized Logging**: Stream JSON logs to an ELK or EFK stack.
