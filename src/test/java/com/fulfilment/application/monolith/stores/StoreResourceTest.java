package com.fulfilment.application.monolith.stores;

import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import jakarta.transaction.Status;
import jakarta.transaction.TransactionSynchronizationRegistry;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.not;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;

@QuarkusTest
public class StoreResourceTest {

    @InjectMock
    LegacyStoreManagerGateway legacyStoreManagerGateway;

    @Inject
    TransactionSynchronizationRegistry transactionSynchronizationRegistry;

    @Test
    public void testGetAllStoresAndDeleteSuccess() {
        given()
                .when().get("/stores")
                .then()
                .statusCode(200)
                .body(containsString("HAARLEM"),
                        containsString("AMSTERDAM"),
                        containsString("HENGELO"));

        given()
                .when().delete("/stores/3")
                .then()
                .statusCode(204);

        given()
                .when().get("/stores")
                .then()
                .statusCode(200)
                .body(containsString("HAARLEM"),
                        containsString("AMSTERDAM"),
                        not(containsString("HENGELO")));
    }

    @Test
    public void testGetSingleStore() {
        given()
                .when().get("/stores/1")
                .then()
                .statusCode(200)
                .body("name", is("HAARLEM"))
                .body("quantityProductsInStock", is(10));
    }

    @Test
    public void testUpdateStoreSuccess() {
        Store updatedStore = new Store();
        updatedStore.name = "HAARLEM UPDATED";
        updatedStore.quantityProductsInStock = 20;

        given()
                .contentType("application/json")
                .body(updatedStore)
                .when().put("/stores/1")
                .then()
                .statusCode(200)
                .body("name", is("HAARLEM UPDATED"))
                .body("quantityProductsInStock", is(20));

        verify(legacyStoreManagerGateway).updateStoreOnLegacySystem(any(Store.class));
    }

    @Test
    public void testPatchStoreSuccess() {
        Store patchStore = new Store();
        patchStore.name = "HAARLEM PATCHED";
        patchStore.quantityProductsInStock = 30;

        given()
                .contentType("application/json")
                .body(patchStore)
                .when().patch("/stores/1")
                .then()
                .statusCode(200)
                .body("name", is("HAARLEM PATCHED"))
                .body("quantityProductsInStock", is(30));

        verify(legacyStoreManagerGateway).updateStoreOnLegacySystem(any(Store.class));
    }

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

    @Test
    public void testGetStoreNotFound() {
        given()
                .when().get("/stores/9999")
                .then()
                .statusCode(404);
    }

    @Test
    public void testCreateStoreWithId() {
        Store store = new Store();
        store.id = 1L;
        store.name = "Invalid Store";

        given()
                .contentType("application/json")
                .body(store)
                .when().post("/stores")
                .then()
                .statusCode(422);
    }

    @Test
    public void testUpdateStoreNotFound() {
        Store store = new Store();
        store.name = "Non-existent Store";

        given()
                .contentType("application/json")
                .body(store)
                .when().put("/stores/9999")
                .then()
                .statusCode(404);
    }

    @Test
    public void testUpdateStoreMissingName() {
        Store store = new Store();
        // name is null

        given()
                .contentType("application/json")
                .body(store)
                .when().put("/stores/1")
                .then()
                .statusCode(422);
    }

    @Test
    public void testPatchStoreNotFound() {
        Store store = new Store();
        store.name = "Non-existent Store";

        given()
                .contentType("application/json")
                .body(store)
                .when().patch("/stores/9999")
                .then()
                .statusCode(404);
    }

    @Test
    public void testDeleteStoreNotFound() {
        given()
                .when().delete("/stores/9999")
                .then()
                .statusCode(404);
    }
}
