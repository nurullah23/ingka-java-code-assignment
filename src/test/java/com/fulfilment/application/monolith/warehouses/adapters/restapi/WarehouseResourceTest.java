package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.warehouse.api.beans.Warehouse;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.not;

@QuarkusTest
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WarehouseResourceTest {

    private static final String path = "warehouse";

    @Test
    @Order(1)
    public void testListAllWarehousesUnits() {
        given()
                .when().get(path)
                .then()
                .statusCode(200)
                .body(containsString("MWH.001"),
                        containsString("MWH.012"),
                        containsString("MWH.023"));
    }

    @Test
    @Order(2)
    public void testGetAWarehouseUnitByID() {
        given()
                .when().get(path + "/MWH.012")
                .then()
                .statusCode(200)
                .body("id", is("MWH.012"))
                .body("location", is("AMSTERDAM-001"))
                .body("capacity", is(50))
                .body("stock", is(5));
    }

    @Test
    @Order(3)
    public void testCreateANewWarehouseUnit() {
        Warehouse warehouse = new Warehouse();
        warehouse.setId("NEW-WH-999");
        warehouse.setLocation("AMSTERDAM-002");
        warehouse.setCapacity(20);
        warehouse.setStock(10);

        given()
                .contentType("application/json")
                .body(warehouse)
                .when().post(path)
                .then()
                .statusCode(200)
                .body("id", is("NEW-WH-999"));
    }

    @Test
    @Order(4)
    public void testArchiveAWarehouseUnitByID() {
        // First verify it exists in the list
        given()
                .when().get(path)
                .then()
                .statusCode(200)
                .body(containsString("MWH.001"));

        // Archive it
        given()
                .when().delete(path + "/MWH.001")
                .then()
                .statusCode(204);

        // Verify it's no longer in the list
        given()
                .when().get(path)
                .then()
                .statusCode(200)
                .body(not(containsString("MWH.001")));
    }

    @Test
    @Order(5)
    public void testGetAWarehouseUnitByIDNotFound() {
        given()
                .when().get(path + "/NON-EXISTENT")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(6)
    public void testArchiveAWarehouseUnitByIDNotFound() {
        given()
                .when().delete(path + "/NON-EXISTENT")
                .then()
                .statusCode(404);
    }
}
