package com.fulfilment.application.monolith.warehouses.adapters.restapi;

import com.fulfilment.application.monolith.warehouses.adapters.database.WarehouseRepository;
import com.fulfilment.application.monolith.warehouses.domain.ports.ArchiveWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.CreateWarehouseOperation;
import com.fulfilment.application.monolith.warehouses.domain.ports.ReplaceWarehouseOperation;
import com.warehouse.api.WarehouseResource;
import com.warehouse.api.beans.Warehouse;
import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.validation.constraints.NotNull;
import org.jboss.logging.Logger;

import java.util.List;

@RequestScoped
public class WarehouseResourceImpl implements WarehouseResource {

  private static final Logger LOGGER = Logger.getLogger(WarehouseResourceImpl.class);

  @Inject private WarehouseRepository warehouseRepository;

  @Inject private CreateWarehouseOperation createWarehouseOperation;

  @Inject private ArchiveWarehouseOperation archiveWarehouseOperation;

  @Inject private ReplaceWarehouseOperation replaceWarehouseOperation;

  @Override
  public List<Warehouse> listAllWarehousesUnits() {
    return warehouseRepository.getAll().stream().map(this::toWarehouseResponse).toList();
  }

  @Override
  public Warehouse createANewWarehouseUnit(@NotNull Warehouse data) {
    LOGGER.infof("Creating a new warehouse unit: %s", data.getId());
    com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse =
        new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    warehouse.businessUnitCode = data.getId();
    warehouse.location = data.getLocation();
    warehouse.capacity = data.getCapacity();
    warehouse.stock = data.getStock();

    createWarehouseOperation.create(warehouse);

    return data;
  }

  @Override
  public Warehouse getAWarehouseUnitByID(String id) {
    LOGGER.infof("Getting warehouse unit by ID: %s", id);
    com.fulfilment.application.monolith.warehouses.domain.models.Warehouse domainWarehouse =
            warehouseRepository.findByInternalId(Long.parseLong(id));
    if (domainWarehouse == null) {
      LOGGER.warnf("Warehouse unit not found: %s", id);
      throw new jakarta.ws.rs.WebApplicationException("Warehouse unit not found", 404);
    }
    Warehouse apiWarehouse = new Warehouse();
    apiWarehouse.setId(id);
    apiWarehouse.setBusinessUnitCode(domainWarehouse.businessUnitCode);
    apiWarehouse.setLocation(domainWarehouse.location);
    apiWarehouse.setCapacity(domainWarehouse.capacity);
    apiWarehouse.setStock(domainWarehouse.stock);
    return apiWarehouse;
  }

  @Override
  public void archiveAWarehouseUnitByID(String id) {
    LOGGER.infof("Archiving warehouse unit by ID: %s", id);
    com.fulfilment.application.monolith.warehouses.domain.models.Warehouse domainWarehouse =
            warehouseRepository.findByInternalId(Long.parseLong(id));
    if (domainWarehouse != null) {
      archiveWarehouseOperation.archive(domainWarehouse);
    } else {
      LOGGER.warnf("Warehouse unit not found for archiving: %s", id);
      throw new jakarta.ws.rs.WebApplicationException("Warehouse unit not found", 404);
    }
  }

  @Override
  public Warehouse replaceTheCurrentActiveWarehouse(
          String businessUnitCode, @NotNull Warehouse data) {
    LOGGER.infof("Replacing warehouse unit: %s", businessUnitCode);
    com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse =
            new com.fulfilment.application.monolith.warehouses.domain.models.Warehouse();
    warehouse.businessUnitCode = businessUnitCode;
    warehouse.location = data.getLocation();
    warehouse.capacity = data.getCapacity();
    warehouse.stock = data.getStock();

    try {
      replaceWarehouseOperation.replace(warehouse);
    } catch (IllegalArgumentException e) {
      if (e.getMessage().contains("not found")) {
        throw new jakarta.ws.rs.WebApplicationException(e.getMessage(), 404);
      }
      throw new jakarta.ws.rs.WebApplicationException(e.getMessage(), 400);
    }

    return data;
  }

  private Warehouse toWarehouseResponse(
          com.fulfilment.application.monolith.warehouses.domain.models.Warehouse warehouse) {
    var response = new Warehouse();
    response.setId(warehouse.id != null ? warehouse.id.toString() : null);
    response.setBusinessUnitCode(warehouse.businessUnitCode);
    response.setLocation(warehouse.location);
    response.setCapacity(warehouse.capacity);
    response.setStock(warehouse.stock);

    return response;
  }
}
