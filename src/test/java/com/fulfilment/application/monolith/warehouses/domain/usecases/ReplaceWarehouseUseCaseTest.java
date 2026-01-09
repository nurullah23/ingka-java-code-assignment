package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ReplaceWarehouseUseCaseTest {

  private WarehouseStore warehouseStore;
  private ReplaceWarehouseUseCase replaceWarehouseUseCase;

  @BeforeEach
  public void setUp() {
    warehouseStore = mock(WarehouseStore.class);
    replaceWarehouseUseCase = new ReplaceWarehouseUseCase(warehouseStore);
  }

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

    verify(warehouseStore).update(newWarehouse);
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

    verify(warehouseStore).update(newWarehouse);
  }

  @Test
  public void testReplaceWarehouseNotFound() {
    Warehouse newWarehouse = new Warehouse();
    newWarehouse.businessUnitCode = "BU001";

    when(warehouseStore.findByBusinessUnitCode("BU001")).thenReturn(null);

    assertThrows(IllegalArgumentException.class, () -> replaceWarehouseUseCase.replace(newWarehouse));
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

    assertThrows(IllegalArgumentException.class, () -> replaceWarehouseUseCase.replace(newWarehouse));
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

    assertThrows(IllegalArgumentException.class, () -> replaceWarehouseUseCase.replace(newWarehouse));
  }

  @Test
  public void testReplaceWarehouseNullInput() {
    assertThrows(NullPointerException.class, () -> replaceWarehouseUseCase.replace(null));
  }

}
