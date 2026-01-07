package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;
import java.time.ZonedDateTime;
import java.util.List;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private static final Logger LOGGER = Logger.getLogger(CreateWarehouseUseCase.class);

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public CreateWarehouseUseCase(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  @Override
  public void create(Warehouse warehouse) {
    LOGGER.infof("Creating warehouse: %s at location: %s", warehouse.businessUnitCode, warehouse.location);
    // Business Unit Code Verification
    if (warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode) != null) {
      LOGGER.warnf("Warehouse with business unit code already exists: %s", warehouse.businessUnitCode);
      throw new IllegalArgumentException("Warehouse with business unit code already exists");
    }

    // Location Validation
    Location location = locationResolver.resolveByIdentifier(warehouse.location);
    if (location == null) {
      LOGGER.warnf("Invalid location: %s", warehouse.location);
      throw new IllegalArgumentException("Invalid location");
    }

    List<Warehouse> existingWarehouses = warehouseStore.findByLocation(warehouse.location);
    long activeWarehousesCount =
        existingWarehouses.stream().filter(w -> w.archivedAt == null).count();

    // Warehouse Creation Feasibility
    if (activeWarehousesCount >= location.maxNumberOfWarehouses) {
      LOGGER.warnf("Maximum number of warehouses reached for location: %s", warehouse.location);
      throw new IllegalStateException("Maximum number of warehouses reached for this location");
    }

    // Capacity and Stock Validation
    int currentTotalCapacity =
        existingWarehouses.stream()
            .filter(w -> w.archivedAt == null)
            .mapToInt(w -> w.capacity)
            .sum();
    if (currentTotalCapacity + warehouse.capacity > location.maxCapacity) {
      LOGGER.warnf("Maximum capacity reached for location: %s", warehouse.location);
      throw new IllegalStateException("Maximum capacity reached for this location");
    }

    if (warehouse.stock > warehouse.capacity) {
      LOGGER.warnf("Stock exceeds capacity for warehouse: %s", warehouse.businessUnitCode);
      throw new IllegalArgumentException("Stock cannot exceed warehouse capacity");
    }

    // if all went well, create the warehouse
    warehouse.creationAt = ZonedDateTime.now();
    warehouseStore.create(warehouse);
    LOGGER.infof("Warehouse created successfully: %s", warehouse.businessUnitCode);
  }
}
