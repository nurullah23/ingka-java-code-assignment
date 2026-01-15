package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
public class CreateWarehouseUseCaseTest {

  @InjectMock
  private WarehouseStore warehouseStore;

  @InjectMock
  private LocationResolver locationResolver;

  @InjectMock
  private WarehouseValidator warehouseValidator;

  @Inject
  private CreateWarehouseUseCase createWarehouseUseCase;

  @Test
  public void testCreateWarehouseSuccess() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "BU001";
    warehouse.location = "LOC001";
    warehouse.capacity = 100;
    warehouse.stock = 50;

    when(warehouseStore.findByBusinessUnitCode("BU001")).thenReturn(null);

    createWarehouseUseCase.create(warehouse);

    verify(warehouseValidator).validate(warehouse, null);
    verify(warehouseStore).create(warehouse);
  }

  @Test
  public void testCreateWarehouseAlreadyExists() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "BU001";

    when(warehouseStore.findByBusinessUnitCode("BU001")).thenReturn(new Warehouse());

    assertThrows(IllegalArgumentException.class, () -> createWarehouseUseCase.create(warehouse));
    verify(warehouseValidator, never()).validate(any(), any());
    verify(warehouseStore, never()).create(any());
  }

  @Test
  public void testCreateWarehouseInvalidLocation() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "BU001";
    warehouse.location = "INVALID";

    when(warehouseStore.findByBusinessUnitCode("BU001")).thenReturn(null);
    doThrow(new IllegalArgumentException("Invalid location"))
        .when(warehouseValidator).validate(warehouse, null);

    assertThrows(IllegalArgumentException.class, () -> createWarehouseUseCase.create(warehouse));
    verify(warehouseValidator).validate(warehouse, null);
    verify(warehouseStore, never()).create(any());
  }

  @Test
  public void testCreateWarehouseMaxWarehousesReached() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "BU001";
    warehouse.location = "LOC001";

    when(warehouseStore.findByBusinessUnitCode("BU001")).thenReturn(null);
    doThrow(new IllegalStateException("Maximum number of warehouses reached for this location"))
        .when(warehouseValidator).validate(warehouse, null);

    assertThrows(IllegalStateException.class, () -> createWarehouseUseCase.create(warehouse));
    verify(warehouseValidator).validate(warehouse, null);
    verify(warehouseStore, never()).create(any());
  }

  @Test
  public void testCreateWarehouseMaxCapacityReached() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "BU001";
    warehouse.location = "LOC001";
    warehouse.capacity = 200;

    when(warehouseStore.findByBusinessUnitCode("BU001")).thenReturn(null);
    doThrow(new IllegalStateException("Maximum capacity reached for this location"))
        .when(warehouseValidator).validate(warehouse, null);

    assertThrows(IllegalStateException.class, () -> createWarehouseUseCase.create(warehouse));
    verify(warehouseValidator).validate(warehouse, null);
    verify(warehouseStore, never()).create(any());
  }

  @Test
  public void testCreateWarehouseStockExceedsCapacity() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "BU001";
    warehouse.location = "LOC001";
    warehouse.capacity = 100;
    warehouse.stock = 150;

    when(warehouseStore.findByBusinessUnitCode("BU001")).thenReturn(null);
    doThrow(new IllegalArgumentException("Stock cannot exceed warehouse capacity"))
        .when(warehouseValidator).validate(warehouse, null);

    assertThrows(IllegalArgumentException.class, () -> createWarehouseUseCase.create(warehouse));
    verify(warehouseValidator).validate(warehouse, null);
    verify(warehouseStore, never()).create(any());
  }

  @Test
  public void testCreateWarehouseStockEqualsCapacity() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "BU001";
    warehouse.location = "LOC001";
    warehouse.capacity = 100;
    warehouse.stock = 100;

    when(warehouseStore.findByBusinessUnitCode("BU001")).thenReturn(null);

    createWarehouseUseCase.create(warehouse);

    verify(warehouseValidator).validate(warehouse, null);
    verify(warehouseStore).create(warehouse);
  }

  @Test
  public void testCreateWarehouseNullInput() {
    assertThrows(NullPointerException.class, () -> createWarehouseUseCase.create(null));
  }

  @Test
  public void testCreateWarehouseMultipleExistingContributingToCapacity() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "BU003";
    warehouse.location = "LOC001";
    warehouse.capacity = 50;

    when(warehouseStore.findByBusinessUnitCode("BU003")).thenReturn(null);
    doThrow(new IllegalStateException("Maximum capacity reached for this location"))
        .when(warehouseValidator).validate(warehouse, null);

    assertThrows(IllegalStateException.class, () -> createWarehouseUseCase.create(warehouse));
    verify(warehouseValidator).validate(warehouse, null);
    verify(warehouseStore, never()).create(any());
  }
}
