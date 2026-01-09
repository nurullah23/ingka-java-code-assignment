package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import com.warehouse.api.WarehouseResource;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import org.jboss.logging.Logger;

import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
public class WarehouseResourceImpl implements WarehouseResource {

  private static final Logger LOGGER = Logger.getLogger(WarehouseResourceImpl.class);

  @Inject WarehouseStore warehouseStore;

  @Inject CreateWarehouseOperation createWarehouseOperation;

  @Inject ArchiveWarehouseOperation archiveWarehouseOperation;

  @Override
  public List<com.warehouse.api.beans.Warehouse> listAllWarehousesUnits() {
    LOGGER.info("Listing all warehouse units");
    return warehouseStore.getAll().stream()
        .filter(domainWarehouse -> domainWarehouse.archivedAt == null)
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
    LOGGER.infof("Creating a new warehouse unit: %s", data.getId());
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
    LOGGER.infof("Getting warehouse unit by ID: %s", id);
    com.fulfilment.application.monolith.warehouses.domain.models.Warehouse domainWarehouse =
        warehouseStore.findByBusinessUnitCode(id);
    if (domainWarehouse == null) {
      LOGGER.warnf("Warehouse unit not found: %s", id);
      throw new jakarta.ws.rs.WebApplicationException("Warehouse unit not found", 404);
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
    LOGGER.infof("Archiving warehouse unit by ID: %s", id);
    com.fulfilment.application.monolith.warehouses.domain.models.Warehouse domainWarehouse =
        warehouseStore.findByBusinessUnitCode(id);
    if (domainWarehouse != null) {
      archiveWarehouseOperation.archive(domainWarehouse);
    } else {
      LOGGER.warnf("Warehouse unit not found for archiving: %s", id);
      throw new jakarta.ws.rs.WebApplicationException("Warehouse unit not found", 404);
    }
  }
}
