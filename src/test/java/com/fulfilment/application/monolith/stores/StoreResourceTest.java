package com.fulfilment.application.monolith.stores;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Status;
import jakarta.transaction.TransactionSynchronizationRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.doAnswer;

@QuarkusTest
public class StoreResourceTest {

    @InjectMock
    LegacyStoreManagerGateway legacyStoreManagerGateway;

    @Inject
    TransactionSynchronizationRegistry transactionSynchronizationRegistry;

    @Test
    public void testCreateStoreCallsLegacySystemAfterCommit() {
        Store store = new Store();
        store.name = "Test Store";
        store.quantityProductsInStock = 10;

        final int[] statusAtCall = {-1};
        doAnswer(invocation -> {
            statusAtCall[0] = transactionSynchronizationRegistry.getTransactionStatus();
            return null;
        }).when(legacyStoreManagerGateway).createStoreOnLegacySystem(any(Store.class));

        given()
          .contentType("application/json")
          .body(store)
          .when().post("/stores")
          .then()
          .statusCode(201)
          .body("name", is("Test Store"));

        verify(legacyStoreManagerGateway).createStoreOnLegacySystem(any(Store.class));
        
        // In afterCompletion(STATUS_COMMITTED), the transaction might already be in STATUS_COMMITTED (3) 
        // or STATUS_NO_TRANSACTION (6) depending on implementation details.
        // Usually, in JTA afterCompletion, the transaction is already completed.
        Assertions.assertTrue(statusAtCall[0] == Status.STATUS_COMMITTED || statusAtCall[0] == Status.STATUS_NO_TRANSACTION, 
            "Transaction should be committed (3) or no transaction (6) but was " + statusAtCall[0]);
    }
}
