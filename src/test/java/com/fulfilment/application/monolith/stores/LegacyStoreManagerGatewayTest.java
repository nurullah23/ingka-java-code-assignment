package com.fulfilment.application.monolith.stores;

import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@QuarkusTest
public class LegacyStoreManagerGatewayTest {

    @Inject
    LegacyStoreManagerGateway legacyStoreManagerGateway;

    @Test
    public void testCreateStoreOnLegacySystem() {
        Store store = new Store();
        store.name = "TestStoreCreate";
        store.quantityProductsInStock = 10;

        assertDoesNotThrow(() -> legacyStoreManagerGateway.createStoreOnLegacySystem(store));
    }

    @Test
    public void testUpdateStoreOnLegacySystem() {
        Store store = new Store();
        store.name = "TestStoreUpdate";
        store.quantityProductsInStock = 20;

        assertDoesNotThrow(() -> legacyStoreManagerGateway.updateStoreOnLegacySystem(store));
    }

    @Test
    public void testWriteToFileHandlesExceptionGracefully() {
        // Passing null might cause a NullPointerException when accessing store.name
        // The catch block in LegacyStoreManagerGateway should handle it.
        assertDoesNotThrow(() -> legacyStoreManagerGateway.createStoreOnLegacySystem(null));
    }
}
