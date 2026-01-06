package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import java.time.ZonedDateTime;
import java.util.List;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public CreateWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  public void create(Warehouse warehouse) {
    // Business Unit Code Verification
    if (warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode) != null) {
      throw new IllegalArgumentException("Warehouse with business unit code already exists");
    }

    // Location Validation
    Location location = locationResolver.resolveByIdentifier(warehouse.location);
    if (location == null) {
      throw new IllegalArgumentException("Invalid location");
    }

    List<Warehouse> existingWarehouses = warehouseStore.findByLocation(warehouse.location);
    long activeWarehousesCount =
        existingWarehouses.stream().filter(w -> w.archivedAt == null).count();

    // Warehouse Creation Feasibility
    if (activeWarehousesCount >= location.maxNumberOfWarehouses) {
      throw new IllegalStateException("Maximum number of warehouses reached for this location");
    }

    // Capacity and Stock Validation
    int currentTotalCapacity =
        existingWarehouses.stream()
            .filter(w -> w.archivedAt == null)
            .mapToInt(w -> w.capacity)
            .sum();
    if (currentTotalCapacity + warehouse.capacity > location.maxCapacity) {
      throw new IllegalStateException("Maximum capacity reached for this location");
    }

    if (warehouse.stock > warehouse.capacity) {
      throw new IllegalArgumentException("Stock cannot exceed warehouse capacity");
    }

    // if all went well, create the warehouse
    warehouse.creationAt = ZonedDateTime.now();
    warehouseStore.create(warehouse);
  }
}
