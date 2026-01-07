package com.fulfilment.application.monolith.infrastructure.rest;

import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.Test;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.is;

@QuarkusTest
public class GlobalExceptionMapperTest {

    @Test
    public void testNotFoundException() {
        given()
          .when().get("/stores/9999")
          .then()
          .statusCode(404)
          .body("code", is(404))
          .body("exceptionType", is("jakarta.ws.rs.WebApplicationException"))
          .body("error", is("Store with id of 9999 does not exist."));
    }

    @Test
    public void testBadRequestException() {
        // Create a store with an ID set, which should trigger a 422 in StoreResource, mapped to 422 by GlobalExceptionMapper
        String storeWithId = "{\"id\": 1, \"name\": \"Store with ID\"}";
        given()
          .contentType("application/json")
          .body(storeWithId)
          .when().post("/stores")
          .then()
          .statusCode(422)
          .body("code", is(422))
          .body("exceptionType", is("jakarta.ws.rs.WebApplicationException"))
          .body("error", is("Id was invalidly set on request."));
    }

    @Test
    public void testIllegalArgumentException() {
        // Warehouse with existing code should throw IllegalArgumentException, mapped to 400
        String warehouseData = "{\"id\": \"MW001\", \"location\": \"AMSTERDAM-001\", \"capacity\": 50, \"stock\": 10}";
        
        // First creation
        given()
          .contentType("application/json")
          .body(warehouseData)
          .when().post("/warehouse")
          .then()
          .statusCode(200);

        // Second creation with same ID
        given()
          .contentType("application/json")
          .body(warehouseData)
          .when().post("/warehouse")
          .then()
          .statusCode(400)
          .body("code", is(400))
          .body("exceptionType", is("java.lang.IllegalArgumentException"))
          .body("error", is("Warehouse with business unit code already exists"));
    }
}
