package com.fulfilment.application.monolith.products;

import io.quarkus.test.TestTransaction;
import io.quarkus.test.junit.QuarkusTest;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.math.BigDecimal;

import static io.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.core.IsNot.not;

@QuarkusTest
@TestTransaction
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ProductEndpointTest {

  @Test
  @Order(1)
  public void testGetAllProducts() {
    final String path = "product";

    // List all, should have all 3 products the database has initially:
    given()
        .when()
        .get(path)
        .then()
        .statusCode(200)
        .body(containsString("TONSTAD"), containsString("KALLAX"), containsString("BESTÅ"));
  }

  @Test
  @Order(2)
  public void testGetSingleProduct() {
    given()
        .when().get("/product/1")
        .then()
        .statusCode(200)
        .body("name", is("TONSTAD"))
        .body("stock", is(10));
  }

  @Test
  @Order(3)
  public void testCreateProduct() {
    Product product = new Product();
    product.name = "New Product";
    product.description = "New Description";
    product.price = new BigDecimal("100.00");
    product.stock = 50;

    given()
        .contentType("application/json")
        .body(product)
        .when().post("/product")
        .then()
        .statusCode(201)
        .body("name", is("New Product"))
        .body("stock", is(50));
  }

  @Test
  @Order(4)
  public void testUpdateProduct() {
    Product product = new Product();
    product.name = "KALLAX UPDATED";
    product.description = "Updated Description";
    product.price = new BigDecimal("50.00");
    product.stock = 20;

    given()
        .contentType("application/json")
        .body(product)
        .when().put("/product/2")
        .then()
        .statusCode(200)
        .body("name", is("KALLAX UPDATED"))
        .body("stock", is(20));
  }

  @Test
  @Order(5)
  public void testDeleteProduct() {
    final String path = "product";

    // Delete the TONSTAD:
    given().when().delete(path + "/1").then().statusCode(204);

    // List all, TONSTAD should be missing now:
    given()
        .when()
        .get(path)
        .then()
        .statusCode(200)
        .body(not(containsString("TONSTAD")), containsString("KALLAX"), containsString("BESTÅ"));
  }

  @Test
  @Order(6)
  public void testGetProductNotFound() {
    given()
            .when().get("/product/9999")
            .then()
            .statusCode(404);
  }

  @Test
  @Order(7)
  public void testCreateProductWithId() {
    Product product = new Product();
    product.id = 1L;
    product.name = "Invalid Product";

    given()
            .contentType("application/json")
            .body(product)
            .when().post("/product")
            .then()
            .statusCode(422);
  }

  @Test
  @Order(8)
  public void testUpdateProductNotFound() {
    Product product = new Product();
    product.name = "Non-existent Product";

    given()
            .contentType("application/json")
            .body(product)
            .when().put("/product/9999")
            .then()
            .statusCode(404);
  }

  @Test
  @Order(9)
  public void testUpdateProductMissingName() {
    Product product = new Product();
    // name is null

    given()
            .contentType("application/json")
            .body(product)
            .when().put("/product/1")
            .then()
            .statusCode(422);
  }

  @Test
  @Order(10)
  public void testDeleteProductNotFound() {
    given()
            .when().delete("/product/9999")
            .then()
            .statusCode(404);
  }
}
