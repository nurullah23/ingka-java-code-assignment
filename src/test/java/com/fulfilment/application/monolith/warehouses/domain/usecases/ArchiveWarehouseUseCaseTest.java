package com.fulfilment.application.monolith.warehouses.domain.usecases;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import java.time.ZonedDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ArchiveWarehouseUseCaseTest {

  private WarehouseStore warehouseStore;
  private ArchiveWarehouseUseCase archiveWarehouseUseCase;

  @BeforeEach
  public void setUp() {
    warehouseStore = mock(WarehouseStore.class);
    archiveWarehouseUseCase = new ArchiveWarehouseUseCase(warehouseStore);
  }

  @Test
  public void testArchiveWarehouseSuccess() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "BU001";

    archiveWarehouseUseCase.archive(warehouse);

    assertNotNull(warehouse.archivedAt);
    verify(warehouseStore).update(warehouse);
  }

  @Test
  public void testArchiveWarehouseAlreadyArchived() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "BU001";
    warehouse.archivedAt = ZonedDateTime.now();

    assertThrows(IllegalStateException.class, () -> archiveWarehouseUseCase.archive(warehouse));
    verify(warehouseStore, never()).update(any());
  }
}
