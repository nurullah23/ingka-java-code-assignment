package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.warehouse.api.WarehouseResource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class WarehouseResourceImpl implements WarehouseResource {

  @Inject WarehouseStore warehouseStore;

  @Inject CreateWarehouseOperation createWarehouseOperation;

  @Inject ArchiveWarehouseOperation archiveWarehouseOperation;

  @Override
  public List<com.warehouse.api.beans.Warehouse> listAllWarehousesUnits() {
    return warehouseStore.getAll().stream()
        .map(
            domainWarehouse -> {
              com.warehouse.api.beans.Warehouse apiWarehouse =
                  new com.warehouse.api.beans.Warehouse();
              apiWarehouse.setId(domainWarehouse.businessUnitCode);
              apiWarehouse.setLocation(domainWarehouse.location);
              apiWarehouse.setCapacity(domainWarehouse.capacity);
              apiWarehouse.setStock(domainWarehouse.stock);
              return apiWarehouse;
            })
        .collect(Collectors.toList());
  }

  @Override
  public com.warehouse.api.beans.Warehouse createANewWarehouseUnit(
      @NotNull com.warehouse.api.beans.Warehouse data) {
    com.fulfilment.application.monolith.warehouses.domain.models.Warehouse domainWarehouse =
        new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    domainWarehouse.businessUnitCode = data.getId();
    domainWarehouse.location = data.getLocation();
    domainWarehouse.capacity = data.getCapacity();
    domainWarehouse.stock = data.getStock();

    createWarehouseOperation.create(domainWarehouse);

    return data;
  }

  @Override
  public com.warehouse.api.beans.Warehouse getAWarehouseUnitByID(String id) {
    com.fulfilment.application.monolith.warehouses.domain.models.Warehouse domainWarehouse =
        warehouseStore.findByBusinessUnitCode(id);
    if (domainWarehouse == null) {
      return null;
    }
    com.warehouse.api.beans.Warehouse apiWarehouse = new com.warehouse.api.beans.Warehouse();
    apiWarehouse.setId(domainWarehouse.businessUnitCode);
    apiWarehouse.setLocation(domainWarehouse.location);
    apiWarehouse.setCapacity(domainWarehouse.capacity);
    apiWarehouse.setStock(domainWarehouse.stock);
    return apiWarehouse;
  }

  @Override
  public void archiveAWarehouseUnitByID(String id) {
    //TODO!!!!!!!!!!!
    com.fulfilment.application.monolith.warehouses.domain.models.Warehouse domainWarehouse =
        warehouseStore.findByBusinessUnitCode(id);
    if (domainWarehouse != null) {
      archiveWarehouseOperation.archive(domainWarehouse);
    }
  }
}
