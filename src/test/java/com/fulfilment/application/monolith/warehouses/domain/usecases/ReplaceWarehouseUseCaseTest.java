package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@QuarkusTest
public class ReplaceWarehouseUseCaseTest {

  @InjectMock
  private WarehouseStore warehouseStore;

  @InjectMock
  private LocationResolver locationResolver;

  @InjectMock
  private WarehouseValidator warehouseValidator;

  @Inject
  private ReplaceWarehouseUseCase replaceWarehouseUseCase;

  @Test
  public void testReplaceWarehouseSuccess() {
    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "BU001";
    newWarehouse.capacity = 100;
    newWarehouse.stock = 50;
    newWarehouse.location = "LOC001";

    Warehouse oldWarehouse = new Warehouse();
    oldWarehouse.businessUnitCode = "BU001";
    oldWarehouse.stock = 50;
    oldWarehouse.location = "LOC001";

    when(warehouseStore.findByBusinessUnitCode("BU001")).thenReturn(oldWarehouse);

    replaceWarehouseUseCase.replace(newWarehouse);

    verify(warehouseValidator).validate(newWarehouse, oldWarehouse);
    verify(warehouseStore).remove(oldWarehouse);
    verify(warehouseStore).create(newWarehouse);
  }

  @Test
  public void testReplaceWarehouseSuccessCapacityEqualsStock() {
    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "BU001";
    newWarehouse.capacity = 50;
    newWarehouse.stock = 50;
    newWarehouse.location = "LOC001";

    Warehouse oldWarehouse = new Warehouse();
    oldWarehouse.businessUnitCode = "BU001";
    oldWarehouse.stock = 50;
    oldWarehouse.location = "LOC001";

    when(warehouseStore.findByBusinessUnitCode("BU001")).thenReturn(oldWarehouse);

    replaceWarehouseUseCase.replace(newWarehouse);

    verify(warehouseValidator).validate(newWarehouse, oldWarehouse);
    verify(warehouseStore).remove(oldWarehouse);
    verify(warehouseStore).create(newWarehouse);
  }

  @Test
  public void testReplaceWarehouseNotFound() {
    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "BU001";

    when(warehouseStore.findByBusinessUnitCode("BU001")).thenReturn(null);

    assertThrows(IllegalArgumentException.class, () -> replaceWarehouseUseCase.replace(newWarehouse));
    verify(warehouseValidator, never()).validate(any(), any());
  }

  @Test
  public void testReplaceWarehouseCapacityTooSmall() {
    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "BU001";
    newWarehouse.capacity = 40;
    newWarehouse.stock = 50;

    Warehouse oldWarehouse = new Warehouse();
    oldWarehouse.businessUnitCode = "BU001";
    oldWarehouse.stock = 50;

    when(warehouseStore.findByBusinessUnitCode("BU001")).thenReturn(oldWarehouse);
    doThrow(new IllegalArgumentException("New capacity cannot accommodate old stock"))
        .when(warehouseValidator).validate(newWarehouse, oldWarehouse);

    assertThrows(IllegalArgumentException.class, () -> replaceWarehouseUseCase.replace(newWarehouse));
    verify(warehouseValidator).validate(newWarehouse, oldWarehouse);
    verify(warehouseStore, never()).remove(any());
  }

  @Test
  public void testReplaceWarehouseStockMismatch() {
    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "BU001";
    newWarehouse.capacity = 100;
    newWarehouse.stock = 60;

    Warehouse oldWarehouse = new Warehouse();
    oldWarehouse.businessUnitCode = "BU001";
    oldWarehouse.stock = 50;

    when(warehouseStore.findByBusinessUnitCode("BU001")).thenReturn(oldWarehouse);
    doThrow(new IllegalArgumentException("Stock of new warehouse must match old stock"))
        .when(warehouseValidator).validate(newWarehouse, oldWarehouse);

    assertThrows(IllegalArgumentException.class, () -> replaceWarehouseUseCase.replace(newWarehouse));
    verify(warehouseValidator).validate(newWarehouse, oldWarehouse);
    verify(warehouseStore, never()).remove(any());
  }

  @Test
  public void testReplaceWarehouseAlreadyArchived() {
    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "BU001";

    Warehouse oldWarehouse = new Warehouse();
    oldWarehouse.businessUnitCode = "BU001";
    oldWarehouse.archivedAt = LocalDateTime.now();

    when(warehouseStore.findByBusinessUnitCode("BU001")).thenReturn(oldWarehouse);
    doThrow(new IllegalStateException("Warehouse is already archived"))
        .when(warehouseValidator).validate(newWarehouse, oldWarehouse);

    assertThrows(IllegalStateException.class, () -> replaceWarehouseUseCase.replace(newWarehouse));
    verify(warehouseValidator).validate(newWarehouse, oldWarehouse);
    verify(warehouseStore, never()).remove(any());
  }

  @Test
  public void testReplaceWarehouseInvalidLocation() {
    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "BU001";
    newWarehouse.location = "INVALID";
    newWarehouse.stock = 50;
    newWarehouse.capacity = 100;

    Warehouse oldWarehouse = new Warehouse();
    oldWarehouse.businessUnitCode = "BU001";
    oldWarehouse.stock = 50;

    when(warehouseStore.findByBusinessUnitCode("BU001")).thenReturn(oldWarehouse);
    doThrow(new IllegalArgumentException("Invalid location"))
        .when(warehouseValidator).validate(newWarehouse, oldWarehouse);

    assertThrows(IllegalArgumentException.class, () -> replaceWarehouseUseCase.replace(newWarehouse));
    verify(warehouseValidator).validate(newWarehouse, oldWarehouse);
    verify(warehouseStore, never()).remove(any());
  }

  @Test
  public void testReplaceWarehouseMaxCapacityReached() {
    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "BU001";
    newWarehouse.location = "LOC001";
    newWarehouse.capacity = 600;
    newWarehouse.stock = 50;

    Warehouse oldWarehouse = new Warehouse();
    oldWarehouse.businessUnitCode = "BU001";
    oldWarehouse.location = "LOC001";
    oldWarehouse.capacity = 100;
    oldWarehouse.stock = 50;

    when(warehouseStore.findByBusinessUnitCode("BU001")).thenReturn(oldWarehouse);
    doThrow(new IllegalStateException("Maximum capacity reached for this location"))
        .when(warehouseValidator).validate(newWarehouse, oldWarehouse);

    assertThrows(IllegalStateException.class, () -> replaceWarehouseUseCase.replace(newWarehouse));
    verify(warehouseValidator).validate(newWarehouse, oldWarehouse);
    verify(warehouseStore, never()).remove(any());
  }

  @Test
  public void testReplaceWarehouseStockExceedsCapacity() {
    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "BU001";
    newWarehouse.location = "LOC001";
    newWarehouse.capacity = 100;
    newWarehouse.stock = 150;

    Warehouse oldWarehouse = new Warehouse();
    oldWarehouse.businessUnitCode = "BU001";
    oldWarehouse.stock = 150;

    when(warehouseStore.findByBusinessUnitCode("BU001")).thenReturn(oldWarehouse);
    doThrow(new IllegalArgumentException("Stock cannot exceed warehouse capacity"))
        .when(warehouseValidator).validate(newWarehouse, oldWarehouse);

    assertThrows(IllegalArgumentException.class, () -> replaceWarehouseUseCase.replace(newWarehouse));
    verify(warehouseValidator).validate(newWarehouse, oldWarehouse);
    verify(warehouseStore, never()).remove(any());
  }

  @Test
  public void testReplaceWarehouseMaxWarehousesReachedInNewLocation() {
    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "BU001";
    newWarehouse.location = "LOC002";
    newWarehouse.capacity = 100;
    newWarehouse.stock = 50;

    Warehouse oldWarehouse = new Warehouse();
    oldWarehouse.businessUnitCode = "BU001";
    oldWarehouse.location = "LOC001";
    oldWarehouse.stock = 50;

    when(warehouseStore.findByBusinessUnitCode("BU001")).thenReturn(oldWarehouse);
    doThrow(new IllegalStateException("Maximum number of warehouses reached for this location"))
        .when(warehouseValidator).validate(newWarehouse, oldWarehouse);

    assertThrows(IllegalStateException.class, () -> replaceWarehouseUseCase.replace(newWarehouse));
    verify(warehouseValidator).validate(newWarehouse, oldWarehouse);
    verify(warehouseStore, never()).remove(any());
  }

  @Test
  public void testReplaceWarehouseNullInput() {
    assertThrows(NullPointerException.class, () -> replaceWarehouseUseCase.replace(null));
  }
}
