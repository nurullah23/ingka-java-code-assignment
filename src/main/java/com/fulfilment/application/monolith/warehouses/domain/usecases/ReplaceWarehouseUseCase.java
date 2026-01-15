package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;
import org.jboss.logging.Logger;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private static final Logger LOGGER = Logger.getLogger(ReplaceWarehouseUseCase.class);

  private final WarehouseStore warehouseStore;
  private final WarehouseValidator warehouseValidator;

  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore, WarehouseValidator warehouseValidator) {
    this.warehouseStore = warehouseStore;
    this.warehouseValidator = warehouseValidator;
  }

  @Override
  public void replace(Warehouse newWarehouse) {
    LOGGER.infof("Replacing warehouse: %s", newWarehouse.businessUnitCode);
    Warehouse oldWarehouse = warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode);
    if (oldWarehouse == null) {
      LOGGER.warnf("Warehouse to replace not found: %s", newWarehouse.businessUnitCode);
      throw new IllegalArgumentException("Warehouse to replace not found");
    }

    warehouseValidator.validate(newWarehouse, oldWarehouse);

    warehouseStore.remove(oldWarehouse);
    warehouseStore.create(newWarehouse);
    LOGGER.infof("Warehouse replaced successfully: %s", newWarehouse.businessUnitCode);
  }
}
