package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CreateWarehouseUseCaseTest {

  private WarehouseStore warehouseStore;
  private LocationResolver locationResolver;
  private CreateWarehouseUseCase createWarehouseUseCase;

  @BeforeEach
  public void setUp() {
    warehouseStore = mock(WarehouseStore.class);
    locationResolver = mock(LocationResolver.class);
    createWarehouseUseCase = new CreateWarehouseUseCase(warehouseStore, locationResolver);
  }

  @Test
  public void testCreateWarehouseSuccess() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "BU001";
    warehouse.location = "LOC001";
    warehouse.capacity = 100;
    warehouse.stock = 50;

    Location location = new Location("LOC001", 5, 1000);

    when(warehouseStore.findByBusinessUnitCode("BU001")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("LOC001")).thenReturn(location);
    when(warehouseStore.findByLocation("LOC001")).thenReturn(List.of());

    createWarehouseUseCase.create(warehouse);

    verify(warehouseStore).create(warehouse);
  }

  @Test
  public void testCreateWarehouseAlreadyExists() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "BU001";

    when(warehouseStore.findByBusinessUnitCode("BU001")).thenReturn(new Warehouse());

    assertThrows(IllegalArgumentException.class, () -> createWarehouseUseCase.create(warehouse));
  }

  @Test
  public void testCreateWarehouseInvalidLocation() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "BU001";
    warehouse.location = "INVALID";

    when(warehouseStore.findByBusinessUnitCode("BU001")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("INVALID")).thenReturn(null);

    assertThrows(IllegalArgumentException.class, () -> createWarehouseUseCase.create(warehouse));
  }

  @Test
  public void testCreateWarehouseMaxWarehousesReached() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "BU001";
    warehouse.location = "LOC001";

    Location location = new Location("LOC001", 1, 1000);

    Warehouse existing = new Warehouse();
    existing.location = "LOC001";
    existing.archivedAt = null;

    Warehouse archived = new Warehouse();
    archived.location = "LOC001";
    archived.archivedAt = LocalDateTime.now();

    when(warehouseStore.findByBusinessUnitCode("BU001")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("LOC001")).thenReturn(location);
    when(warehouseStore.findByLocation("LOC001")).thenReturn(List.of(existing, archived));

    assertThrows(IllegalStateException.class, () -> createWarehouseUseCase.create(warehouse));
  }

  @Test
  public void testCreateWarehouseMaxCapacityReached() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "BU001";
    warehouse.location = "LOC001";
    warehouse.capacity = 200;

    Location location = new Location("LOC001", 5, 300);

    Warehouse existing = new Warehouse();
    existing.location = "LOC001";
    existing.capacity = 150;
    existing.archivedAt = null;

    Warehouse archived = new Warehouse();
    archived.location = "LOC001";
    archived.capacity = 200;
    archived.archivedAt = LocalDateTime.now();

    when(warehouseStore.findByBusinessUnitCode("BU001")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("LOC001")).thenReturn(location);
    when(warehouseStore.findByLocation("LOC001")).thenReturn(List.of(existing, archived));

    assertThrows(IllegalStateException.class, () -> createWarehouseUseCase.create(warehouse));
  }

  @Test
  public void testCreateWarehouseStockExceedsCapacity() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "BU001";
    warehouse.location = "LOC001";
    warehouse.capacity = 100;
    warehouse.stock = 150;

    Location location = new Location("LOC001", 5, 1000);

    when(warehouseStore.findByBusinessUnitCode("BU001")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("LOC001")).thenReturn(location);
    when(warehouseStore.findByLocation("LOC001")).thenReturn(List.of());

    assertThrows(IllegalArgumentException.class, () -> createWarehouseUseCase.create(warehouse));
  }

  @Test
  public void testCreateWarehouseStockEqualsCapacity() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "BU001";
    warehouse.location = "LOC001";
    warehouse.capacity = 100;
    warehouse.stock = 100;

    Location location = new Location("LOC001", 5, 1000);

    when(warehouseStore.findByBusinessUnitCode("BU001")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("LOC001")).thenReturn(location);
    when(warehouseStore.findByLocation("LOC001")).thenReturn(List.of());

    createWarehouseUseCase.create(warehouse);

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

    Location location = new Location("LOC001", 5, 100);

    Warehouse existing1 = new Warehouse();
    existing1.capacity = 30;
    existing1.archivedAt = null;

    Warehouse existing2 = new Warehouse();
    existing2.capacity = 30;
    existing2.archivedAt = null;

    when(warehouseStore.findByBusinessUnitCode("BU003")).thenReturn(null);
    when(locationResolver.resolveByIdentifier("LOC001")).thenReturn(location);
    when(warehouseStore.findByLocation("LOC001")).thenReturn(List.of(existing1, existing2));

    assertThrows(IllegalStateException.class, () -> createWarehouseUseCase.create(warehouse));
  }
}
