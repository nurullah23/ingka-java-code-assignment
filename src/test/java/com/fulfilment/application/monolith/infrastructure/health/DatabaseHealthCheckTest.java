package com.fulfilment.application.monolith.infrastructure.health;
import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class DatabaseHealthCheckTest {

    @Test
    public void testDatabaseHealthCheck() {
        DatabaseHealthCheck healthCheck = new DatabaseHealthCheck();
        HealthCheckResponse response = healthCheck.call();

        assertEquals(HealthCheckResponse.Status.UP, response.getStatus());
        assertEquals("Database connection is healthy", response.getName());
    }
}
