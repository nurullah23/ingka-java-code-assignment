package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;
import org.jboss.logging.Logger;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
@Transactional
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  private static final Logger LOGGER = Logger.getLogger(WarehouseRepository.class);

  @Override
  public void create(Warehouse warehouse) {
    LOGGER.infof("Creating warehouse unit in database: %s", warehouse.businessUnitCode);
    persist(toDb(warehouse));
  }

  @Override
  public void update(Warehouse warehouse) {
    LOGGER.infof("Updating warehouse unit in database: %s", warehouse.businessUnitCode);
    DbWarehouse dbWarehouse = null;
    if (warehouse.id != null) {
      dbWarehouse = findById(warehouse.id);
    } else if (warehouse.businessUnitCode != null) {
      dbWarehouse = find("businessUnitCode", warehouse.businessUnitCode).firstResult();
    }

    if (dbWarehouse != null) {
      dbWarehouse.location = warehouse.location;
      dbWarehouse.capacity = warehouse.capacity;
      dbWarehouse.stock = warehouse.stock;
      dbWarehouse.archivedAt = warehouse.archivedAt;
      persist(dbWarehouse);
    } else {
      LOGGER.warnf("Warehouse unit not found for update: %s", warehouse.businessUnitCode);
    }
  }

  @Override
  public void remove(Warehouse warehouse) {
    LOGGER.infof("Removing (archiving) warehouse unit from database: %s", warehouse.businessUnitCode);
    warehouse.archivedAt = LocalDateTime.now();
    update(warehouse);
  }

  @Override
  public Warehouse findByInternalId(Long id) {
    LOGGER.debugf("Finding warehouse by ID: %d", id);
    DbWarehouse dbWarehouse = findById(id);
    return dbWarehouse != null ? fromDb(dbWarehouse) : null;
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    LOGGER.debugf("Finding warehouse by business unit code: %s", buCode);
    return find("businessUnitCode = ?1 and archivedAt is null", buCode).firstResultOptional().map(this::fromDb).orElse(null);
  }

  @Override
  public List<Warehouse> findByLocation(String locationIdentifier) {
    LOGGER.debugf("Finding warehouses by location: %s", locationIdentifier);
    return find("location", locationIdentifier).stream().map(this::fromDb).collect(Collectors.toList());
  }

  @Override
  public List<Warehouse> getAll() {
    LOGGER.debug("Getting all warehouses from database");
    return list("archivedAt is null").stream().map(this::fromDb).collect(Collectors.toList());
  }

  private DbWarehouse toDb(Warehouse warehouse) {
    DbWarehouse db = new DbWarehouse();
    db.id = warehouse.id;
    db.businessUnitCode = warehouse.businessUnitCode;
    db.location = warehouse.location;
    db.capacity = warehouse.capacity;
    db.stock = warehouse.stock;
    db.createdAt = warehouse.createdAt;
    db.archivedAt = warehouse.archivedAt;
    return db;
  }

  private Warehouse fromDb(DbWarehouse db) {
    Warehouse warehouse = new Warehouse();
    warehouse.id = db.id;
    warehouse.businessUnitCode = db.businessUnitCode;
    warehouse.location = db.location;
    warehouse.capacity = db.capacity;
    warehouse.stock = db.stock;
    warehouse.createdAt = db.createdAt;
    warehouse.archivedAt = db.archivedAt;
    return warehouse;
  }
}
