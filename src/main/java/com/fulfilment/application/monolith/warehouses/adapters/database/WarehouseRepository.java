package com.fulfilment.application.monolith.warehouses.adapters.database;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.hibernate.orm.panache.PanacheRepository;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.stream.Collectors;

@ApplicationScoped
@Transactional
public class WarehouseRepository implements WarehouseStore, PanacheRepository<DbWarehouse> {

  @Override
  public void create(Warehouse warehouse) {
    persist(toDb(warehouse));
  }

  @Override
  public void update(Warehouse warehouse) {
    DbWarehouse dbWarehouse = find("businessUnitCode", warehouse.businessUnitCode).firstResult();
    if (dbWarehouse != null) {
      dbWarehouse.location = warehouse.location;
      dbWarehouse.capacity = warehouse.capacity;
      dbWarehouse.stock = warehouse.stock;
      dbWarehouse.archivedAt =
          warehouse.archivedAt != null ? warehouse.archivedAt.toLocalDateTime() : null;
      persist(dbWarehouse);
    }
  }

  @Override
  public void remove(Warehouse warehouse) {
    warehouse.archivedAt = ZonedDateTime.now();
    update(warehouse);
  }

  @Override
  public Warehouse findByBusinessUnitCode(String buCode) {
    return find("businessUnitCode", buCode).stream().map(this::fromDb).findFirst().orElse(null);
  }

  @Override
  public List<Warehouse> findByLocation(String locationIdentifier) {
    return find("location", locationIdentifier).stream().map(this::fromDb).collect(Collectors.toList());
  }

  @Override
  public List<Warehouse> getAll() {
    return listAll().stream().map(this::fromDb).collect(Collectors.toList());
  }

  private DbWarehouse toDb(Warehouse warehouse) {
    DbWarehouse db = new DbWarehouse();
    db.businessUnitCode = warehouse.businessUnitCode;
    db.location = warehouse.location;
    db.capacity = warehouse.capacity;
    db.stock = warehouse.stock;
    db.createdAt = warehouse.creationAt != null ? warehouse.creationAt.toLocalDateTime() : null;
    db.archivedAt = warehouse.archivedAt != null ? warehouse.archivedAt.toLocalDateTime() : null;
    return db;
  }

  private Warehouse fromDb(DbWarehouse db) {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = db.businessUnitCode;
    warehouse.location = db.location;
    warehouse.capacity = db.capacity;
    warehouse.stock = db.stock;
    warehouse.creationAt = db.createdAt != null ? db.createdAt.atZone(ZoneOffset.UTC) : null;
    warehouse.archivedAt = db.archivedAt != null ? db.archivedAt.atZone(ZoneOffset.UTC) : null;
    return warehouse;
  }
}
