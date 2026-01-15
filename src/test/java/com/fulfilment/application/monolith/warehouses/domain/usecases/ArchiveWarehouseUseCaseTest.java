package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@QuarkusTest
public class ArchiveWarehouseUseCaseTest {

  @InjectMock
  private WarehouseStore warehouseStore;

  @InjectMock
  private WarehouseValidator warehouseValidator;

  @InjectMock
  private LocationResolver locationResolver;

  @Inject
  private ArchiveWarehouseUseCase archiveWarehouseUseCase;

  @Test
  public void testArchiveWarehouseSuccess() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "BU001";

    archiveWarehouseUseCase.archive(warehouse);

    verify(warehouseValidator).validateNotArchived(warehouse);
    verify(warehouseStore).remove(warehouse);
  }

  @Test
  public void testArchiveWarehouseAlreadyArchived() {
    Warehouse warehouse = new Warehouse();
    warehouse.businessUnitCode = "BU001";
    warehouse.archivedAt = LocalDateTime.now();

    doThrow(new IllegalStateException("Warehouse is already archived"))
        .when(warehouseValidator).validateNotArchived(warehouse);

    assertThrows(IllegalStateException.class, () -> archiveWarehouseUseCase.archive(warehouse));
    verify(warehouseValidator).validateNotArchived(warehouse);
    verify(warehouseStore, never()).remove(any());
  }

  @Test
  public void testArchiveWarehouseNullInput() {
    doThrow(new NullPointerException()).when(warehouseValidator).validateNotArchived(null);
    assertThrows(NullPointerException.class, () -> archiveWarehouseUseCase.archive(null));
  }
}
