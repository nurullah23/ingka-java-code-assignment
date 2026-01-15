package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

@ApplicationScoped
public class CreateWarehouseUseCase implements CreateWarehouseOperation {

  private static final Logger LOGGER = Logger.getLogger(CreateWarehouseUseCase.class);

  private final WarehouseStore warehouseStore;
  private final WarehouseValidator warehouseValidator;

  public CreateWarehouseUseCase(WarehouseStore warehouseStore, WarehouseValidator warehouseValidator) {
    this.warehouseStore = warehouseStore;
    this.warehouseValidator = warehouseValidator;
  }

  @Override
  public void create(Warehouse warehouse) {
    LOGGER.infof("Creating warehouse: %s at location: %s", warehouse.businessUnitCode, warehouse.location);
    // Business Unit Code Verification
    if (warehouseStore.findByBusinessUnitCode(warehouse.businessUnitCode) != null) {
      LOGGER.warnf("Warehouse with business unit code already exists: %s", warehouse.businessUnitCode);
      throw new IllegalArgumentException("Warehouse with business unit code already exists");
    }

    warehouseValidator.validate(warehouse, null);

    // if all went well, create the warehouse
    warehouseStore.create(warehouse);
    LOGGER.infof("Warehouse created successfully: %s", warehouse.businessUnitCode);
  }
}
