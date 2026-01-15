package com.fulfilment.application.monolith.infrastructure.health;

import io.quarkus.test.junit.QuarkusTest;
import org.eclipse.microprofile.health.HealthCheckResponse;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

@QuarkusTest
public class SimpleLivenessCheckTest {

    @Test
    public void testSimpleLivenessCheck() {
        SimpleLivenessCheck healthCheck = new SimpleLivenessCheck();
        HealthCheckResponse response = healthCheck.call();

        assertEquals(HealthCheckResponse.Status.UP, response.getStatus());
        assertEquals("Liveness check", response.getName());
    }
}
