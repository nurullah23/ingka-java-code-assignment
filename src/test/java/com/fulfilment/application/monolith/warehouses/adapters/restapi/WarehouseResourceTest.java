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
                .when().get(path + "/1")
                .then()
                .statusCode(200)
                .body(containsString("MWH.001"));
    }

    @Test
    @Order(3)
    public void testCreateANewWarehouseUnit() {
        Warehouse warehouse = new Warehouse();
        warehouse.setBusinessUnitCode("NEW-WH-999");
        warehouse.setLocation("AMSTERDAM-002");
        warehouse.setCapacity(20);
        warehouse.setStock(10);

        given()
                .contentType("application/json")
                .body(warehouse)
                .when().post(path)
                .then()
                .statusCode(200)
                .body("businessUnitCode", is("NEW-WH-999"));
    }

    @Test
    @Order(4)
    public void testArchiveAWarehouseUnitByID() {
        given()
                .when().delete(path + "/1")
                .then()
                .statusCode(204)
                .body(not(containsString("MWH.001")));
    }

    @Test
    @Order(5)
    public void testGetAWarehouseUnitByIDNotFound() {
        given()
                .when().get(path + "/99999")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(6)
    public void testArchiveAWarehouseUnitByIDNotFound() {
        given()
                .when().delete(path + "/99999")
                .then()
                .statusCode(404);
    }
    @Test
    @Order(7)
    public void testReplaceTheCurrentActiveWarehouse() {
        Warehouse warehouse = new Warehouse();
        warehouse.setBusinessUnitCode("MWH.012");
        warehouse.setLocation("AMSTERDAM-001");
        warehouse.setCapacity(100);
        warehouse.setStock(5);

        given()
                .contentType("application/json")
                .body(warehouse)
                .when().post(path + "/MWH.012/replacement")
                .then()
                .statusCode(200)
                .body("businessUnitCode", is("MWH.012"))
                .body("capacity", is(100));
    }

    @Test
    @Order(8)
    public void testReplaceTheCurrentActiveWarehouseNotFound() {
        Warehouse warehouse = new Warehouse();
        warehouse.setBusinessUnitCode("NON-EXISTENT");
        warehouse.setLocation("AMSTERDAM-001");
        warehouse.setCapacity(100);
        warehouse.setStock(0);

        given()
                .contentType("application/json")
                .body(warehouse)
                .when().post(path + "/NON-EXISTENT/replacement")
                .then()
                .statusCode(404);
    }

    @Test
    @Order(9)
    public void testReplaceTheCurrentActiveWarehouseBadRequest() {
        Warehouse warehouse = new Warehouse();
        warehouse.setBusinessUnitCode("MWH.012");
        warehouse.setLocation("AMSTERDAM-001");
        warehouse.setCapacity(1); // Too small
        warehouse.setStock(5);

        given()
                .contentType("application/json")
                .body(warehouse)
                .when().post(path + "/MWH.012/replacement")
                .then()
                .statusCode(400);
    }
}
