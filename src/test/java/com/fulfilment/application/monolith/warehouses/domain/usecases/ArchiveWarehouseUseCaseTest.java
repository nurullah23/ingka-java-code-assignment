package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

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

    verify(warehouseStore).remove(warehouse);
  }

  @Test
  public void testArchiveWarehouseAlreadyArchived() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "BU001";
    warehouse.archivedAt = LocalDateTime.now();

    assertThrows(IllegalStateException.class, () -> archiveWarehouseUseCase.archive(warehouse));
    verify(warehouseStore, never()).remove(any());
  }

  @Test
  public void testArchiveWarehouseNullInput() {
    assertThrows(NullPointerException.class, () -> archiveWarehouseUseCase.archive(null));
  }
}
