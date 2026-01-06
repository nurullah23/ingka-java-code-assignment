package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import jakarta.enterprise.context.ApplicationScoped;

@ApplicationScoped
public class ReplaceWarehouseUseCase implements ReplaceWarehouseOperation {

  private final WarehouseStore warehouseStore;

  public ReplaceWarehouseUseCase(WarehouseStore warehouseStore) {
    this.warehouseStore = warehouseStore;
  }

  @Override
  public void replace(Warehouse newWarehouse) {
    Warehouse oldWarehouse = warehouseStore.findByBusinessUnitCode(newWarehouse.businessUnitCode);
    if (oldWarehouse == null) {
      throw new IllegalArgumentException("Warehouse to replace not found");
    }

    // Capacity Accommodation
    if (newWarehouse.capacity < oldWarehouse.stock) {
      throw new IllegalArgumentException("New capacity cannot accommodate old stock");
    }

    // Stock Matching
    if (!newWarehouse.stock.equals(oldWarehouse.stock)) {
      throw new IllegalArgumentException("Stock of new warehouse must match old stock");
    }

    warehouseStore.update(newWarehouse);
  }
}
