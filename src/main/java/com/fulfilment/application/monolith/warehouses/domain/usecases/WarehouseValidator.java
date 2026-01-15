package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

import java.util.List;

@ApplicationScoped
public class WarehouseValidator {

  private static final Logger LOGGER = Logger.getLogger(WarehouseValidator.class);

  private final WarehouseStore warehouseStore;
  private final LocationResolver locationResolver;

  public WarehouseValidator(WarehouseStore warehouseStore, LocationResolver locationResolver) {
    this.warehouseStore = warehouseStore;
    this.locationResolver = locationResolver;
  }

  public void validate(Warehouse warehouse, Warehouse existingWarehouseBeingReplaced) {
    if (existingWarehouseBeingReplaced != null) {
      validateNotArchived(existingWarehouseBeingReplaced);
    }

    // Location Validation
    Location location = locationResolver.resolveByIdentifier(warehouse.location);
    if (location == null) {
      LOGGER.warnf("Invalid location: %s", warehouse.location);
      throw new IllegalArgumentException("Invalid location");
    }

    List<Warehouse> existingWarehouses = warehouseStore.findByLocation(warehouse.location);

    // Warehouse Creation Feasibility
    // If it's a new warehouse OR the location is changing during replacement
    if (existingWarehouseBeingReplaced == null || !warehouse.location.equals(existingWarehouseBeingReplaced.location)) {
      long activeWarehousesCount =
          existingWarehouses.stream().filter(w -> w.archivedAt == null).count();

      if (activeWarehousesCount >= location.maxNumberOfWarehouses) {
        LOGGER.warnf("Maximum number of warehouses reached for location: %s", warehouse.location);
        throw new IllegalStateException("Maximum number of warehouses reached for this location");
      }
    }

    // Capacity and Stock Validation
    int currentTotalCapacity =
        existingWarehouses.stream()
            .filter(w -> w.archivedAt == null)
            .filter(w -> existingWarehouseBeingReplaced == null || !w.businessUnitCode.equals(existingWarehouseBeingReplaced.businessUnitCode))
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

    if (existingWarehouseBeingReplaced != null) {
      // Capacity Accommodation
      if (warehouse.capacity < existingWarehouseBeingReplaced.stock) {
        LOGGER.warnf("New capacity cannot accommodate old stock for warehouse: %s", warehouse.businessUnitCode);
        throw new IllegalArgumentException("New capacity cannot accommodate old stock");
      }

      // Stock Matching
      if (!warehouse.stock.equals(existingWarehouseBeingReplaced.stock)) {
        LOGGER.warnf("Stock of new warehouse does not match old stock for warehouse: %s", warehouse.businessUnitCode);
        throw new IllegalArgumentException("Stock of new warehouse must match old stock");
      }
    }
  }

  public void validateNotArchived(Warehouse warehouse) {
    if (warehouse.archivedAt != null) {
      LOGGER.warnf("Warehouse already archived: %s", warehouse.businessUnitCode);
      throw new IllegalStateException("Warehouse is already archived");
    }
  }
}
