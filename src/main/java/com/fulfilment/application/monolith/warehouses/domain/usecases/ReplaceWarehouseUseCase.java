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

  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore) {
    this.warehouseStore = warehouseStore;
  }

  @Override
  public void replace(Warehouse newWarehouse) {
    LOGGER.infof("Replacing warehouse: %s", newWarehouse.businessUnitCode);
    Warehouse oldWarehouse = warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode);
    if (oldWarehouse == null) {
      LOGGER.warnf("Warehouse to replace not found: %s", newWarehouse.businessUnitCode);
      throw new IllegalArgumentException("Warehouse to replace not found");
    }

    // Capacity Accommodation
    if (newWarehouse.capacity < oldWarehouse.stock) {
      LOGGER.warnf("New capacity cannot accommodate old stock for warehouse: %s", newWarehouse.businessUnitCode);
      throw new IllegalArgumentException("New capacity cannot accommodate old stock");
    }

    // Stock Matching
    if (!newWarehouse.stock.equals(oldWarehouse.stock)) {
      LOGGER.warnf("Stock of new warehouse does not match old stock for warehouse: %s", newWarehouse.businessUnitCode);
      throw new IllegalArgumentException("Stock of new warehouse must match old stock");
    }

    warehouseStore.remove(oldWarehouse);
    warehouseStore.create(newWarehouse);
    LOGGER.infof("Warehouse replaced successfully: %s", newWarehouse.businessUnitCode);
  }
}
