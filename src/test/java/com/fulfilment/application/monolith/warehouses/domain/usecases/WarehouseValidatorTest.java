package com.fulfilment.application.monolith.warehouses.domain.usecases;

import com.fulfilment.application.monolith.warehouses.domain.models.Location;
import com.fulfilment.application.monolith.warehouses.domain.models.Warehouse;
import com.fulfilment.application.monolith.warehouses.domain.ports.LocationResolver;
import com.fulfilment.application.monolith.warehouses.domain.ports.WarehouseStore;
import io.quarkus.test.InjectMock;
import io.quarkus.test.junit.QuarkusTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@QuarkusTest
public class WarehouseValidatorTest {

    @InjectMock
    private WarehouseStore warehouseStore;

    @InjectMock
    private LocationResolver locationResolver;

    @Inject
    private WarehouseValidator warehouseValidator;

    private Location defaultLocation;

    @BeforeEach
    public void setup() {
        defaultLocation = new Location("LOC1", 2, 1000);
        when(locationResolver.resolveByIdentifier("LOC1")).thenReturn(defaultLocation);
    }

    @Test
    public void testValidateSuccess() {
        Warehouse warehouse = createWarehouse("BU1", "LOC1", 100, 50);
        when(warehouseStore.findByLocation("LOC1")).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> warehouseValidator.validate(warehouse, null));
    }

    @Test
    public void testValidateInvalidLocation() {
        Warehouse warehouse = createWarehouse("BU1", "INVALID", 100, 50);
        when(locationResolver.resolveByIdentifier("INVALID")).thenReturn(null);

        assertThrows(IllegalArgumentException.class, () -> warehouseValidator.validate(warehouse, null));
    }

    @Test
    public void testValidateMaxWarehousesReached() {
        Warehouse warehouse = createWarehouse("BU3", "LOC1", 100, 50);
        Warehouse w1 = createWarehouse("BU1", "LOC1", 100, 50);
        Warehouse w2 = createWarehouse("BU2", "LOC1", 100, 50);
        when(warehouseStore.findByLocation("LOC1")).thenReturn(List.of(w1, w2));

        assertThrows(IllegalStateException.class, () -> warehouseValidator.validate(warehouse, null));
    }

    @Test
    public void testValidateReplacementSameLocationDoesNotCheckMaxWarehouses() {
        Warehouse oldWarehouse = createWarehouse("BU1", "LOC1", 100, 50);
        Warehouse newWarehouse = createWarehouse("BU1", "LOC1", 200, 50);
        
        // Even if max warehouses is reached, same location replacement should be fine
        defaultLocation.maxNumberOfWarehouses = 1;
        when(warehouseStore.findByLocation("LOC1")).thenReturn(List.of(oldWarehouse));

        assertDoesNotThrow(() -> warehouseValidator.validate(newWarehouse, oldWarehouse));
    }

    @Test
    public void testValidateMaxCapacityReached() {
        Warehouse warehouse = createWarehouse("BU2", "LOC1", 600, 50);
        Warehouse w1 = createWarehouse("BU1", "LOC1", 500, 50);
        when(warehouseStore.findByLocation("LOC1")).thenReturn(List.of(w1));

        assertThrows(IllegalStateException.class, () -> warehouseValidator.validate(warehouse, null));
    }

    @Test
    public void testValidateStockExceedsCapacity() {
        Warehouse warehouse = createWarehouse("BU1", "LOC1", 100, 150);
        when(warehouseStore.findByLocation("LOC1")).thenReturn(Collections.emptyList());

        assertThrows(IllegalArgumentException.class, () -> warehouseValidator.validate(warehouse, null));
    }

    @Test
    public void testValidateReplacementCapacityTooSmall() {
        Warehouse oldWarehouse = createWarehouse("BU1", "LOC1", 100, 80);
        Warehouse newWarehouse = createWarehouse("BU1", "LOC1", 50, 80);
        when(warehouseStore.findByLocation("LOC1")).thenReturn(List.of(oldWarehouse));

        assertThrows(IllegalArgumentException.class, () -> warehouseValidator.validate(newWarehouse, oldWarehouse));
    }

    @Test
    public void testValidateReplacementStockMismatch() {
        Warehouse oldWarehouse = createWarehouse("BU1", "LOC1", 100, 50);
        Warehouse newWarehouse = createWarehouse("BU1", "LOC1", 100, 60);
        when(warehouseStore.findByLocation("LOC1")).thenReturn(List.of(oldWarehouse));

        assertThrows(IllegalArgumentException.class, () -> warehouseValidator.validate(newWarehouse, oldWarehouse));
    }

    @Test
    public void testValidateReplacementExistingArchived() {
        Warehouse oldWarehouse = createWarehouse("BU1", "LOC1", 100, 50);
        oldWarehouse.archivedAt = LocalDateTime.now();
        Warehouse newWarehouse = createWarehouse("BU1", "LOC1", 100, 50);

        assertThrows(IllegalStateException.class, () -> warehouseValidator.validate(newWarehouse, oldWarehouse));
    }

    @Test
    public void testValidateNotArchivedSuccess() {
        Warehouse warehouse = createWarehouse("BU1", "LOC1", 100, 50);
        assertDoesNotThrow(() -> warehouseValidator.validateNotArchived(warehouse));
    }

    @Test
    public void testValidateNotArchivedFailure() {
        Warehouse warehouse = createWarehouse("BU1", "LOC1", 100, 50);
        warehouse.archivedAt = LocalDateTime.now();
        assertThrows(IllegalStateException.class, () -> warehouseValidator.validateNotArchived(warehouse));
    }

    @Test
    public void testValidateReplacementDifferentLocationChecksMaxWarehouses() {
        Warehouse oldWarehouse = createWarehouse("BU1", "LOC2", 100, 50);
        Warehouse newWarehouse = createWarehouse("BU1", "LOC1", 100, 50);

        Warehouse w1 = createWarehouse("BU2", "LOC1", 100, 50);
        Warehouse w2 = createWarehouse("BU3", "LOC1", 100, 50);
        when(warehouseStore.findByLocation("LOC1")).thenReturn(List.of(w1, w2));

        assertThrows(IllegalStateException.class, () -> warehouseValidator.validate(newWarehouse, oldWarehouse));
    }

    @Test
    public void testValidateCapacityCalculationExcludesReplacedWarehouse() {
        // Test that when replacing, the old warehouse's capacity is NOT included in the currentTotalCapacity
        Warehouse oldWarehouse = createWarehouse("BU1", "LOC1", 500, 50);
        Warehouse newWarehouse = createWarehouse("BU1", "LOC1", 600, 50);

        // Location has maxCapacity 1000.
        // Existing warehouses at LOC1: oldWarehouse(500) and anotherWarehouse(400)
        Warehouse anotherWarehouse = createWarehouse("BU2", "LOC1", 400, 50);
        when(warehouseStore.findByLocation("LOC1")).thenReturn(List.of(oldWarehouse, anotherWarehouse));

        // If oldWarehouse is excluded: 400 + 600 = 1000 (Success)
        // If oldWarehouse is NOT excluded: 500 + 400 + 600 = 1500 (Failure)
        assertDoesNotThrow(() -> warehouseValidator.validate(newWarehouse, oldWarehouse));
    }

    @Test
    public void testValidateCapacityCalculationIncludesOtherWarehouses() {
        Warehouse warehouse = createWarehouse("BU3", "LOC1", 601, 50);
        Warehouse w1 = createWarehouse("BU1", "LOC1", 200, 50);
        Warehouse w2 = createWarehouse("BU2", "LOC1", 200, 50);
        when(warehouseStore.findByLocation("LOC1")).thenReturn(List.of(w1, w2));

        // 200 + 200 + 601 = 1001 > 1000
        assertThrows(IllegalStateException.class, () -> warehouseValidator.validate(warehouse, null));
    }

    @Test
    public void testValidateCapacityCalculationExcludesReplacedWarehouseWithDifferentBuCode() {
        // This is a special case to cover: 
        // .filter(w -> existingWarehouseBeingReplaced == null || !w.businessUnitCode.equals(existingWarehouseBeingReplaced.businessUnitCode))
        // where existingWarehouseBeingReplaced != null AND w.businessUnitCode EQUALS existingWarehouseBeingReplaced.businessUnitCode
        
        Warehouse oldWarehouse = createWarehouse("BU1", "LOC1", 500, 50);
        Warehouse newWarehouse = createWarehouse("BU1", "LOC1", 500, 50);
        
        // We have BU1 (being replaced) and BU2.
        Warehouse w2 = createWarehouse("BU2", "LOC1", 400, 50);
        
        when(warehouseStore.findByLocation("LOC1")).thenReturn(List.of(oldWarehouse, w2));
        
        // Capacity: newWarehouse(500) + w2(400) = 900 <= 1000. 
        // oldWarehouse(500) is filtered out by the BU code check.
        assertDoesNotThrow(() -> warehouseValidator.validate(newWarehouse, oldWarehouse));
    }

    @Test
    public void testValidateCapacityCalculationIncludesOtherWarehouseWithDifferentBuCode() {
        Warehouse oldWarehouse = createWarehouse("BU1", "LOC1", 100, 50);
        Warehouse newWarehouse = createWarehouse("BU1", "LOC1", 100, 50);
        
        // w2 has different BU code, so it should be included
        Warehouse w2 = createWarehouse("BU2", "LOC1", 901, 50);
        
        when(warehouseStore.findByLocation("LOC1")).thenReturn(List.of(oldWarehouse, w2));
        
        // Capacity: newWarehouse(100) + w2(901) = 1001 > 1000
        assertThrows(IllegalStateException.class, () -> warehouseValidator.validate(newWarehouse, oldWarehouse));
    }

    @Test
    public void testValidateCapacityCalculationWithArchivedWarehouses() {
        Warehouse warehouse = createWarehouse("BU1", "LOC1", 500, 50);
        
        Warehouse wArchived = createWarehouse("BU_OLD", "LOC1", 600, 50);
        wArchived.archivedAt = LocalDateTime.now();
        
        when(warehouseStore.findByLocation("LOC1")).thenReturn(List.of(wArchived));
        
        // Archived warehouse should be filtered out, so 500 <= 1000
        assertDoesNotThrow(() -> warehouseValidator.validate(warehouse, null));
    }

    @Test
    public void testValidateCapacityCalculationExcludesArchivedDuringReplacement() {
        Warehouse oldWarehouse = createWarehouse("BU1", "LOC1", 100, 50);
        Warehouse newWarehouse = createWarehouse("BU1", "LOC1", 100, 50);
        
        Warehouse wArchived = createWarehouse("BU_OLD", "LOC1", 600, 50);
        wArchived.archivedAt = LocalDateTime.now();
        
        when(warehouseStore.findByLocation("LOC1")).thenReturn(List.of(oldWarehouse, wArchived));
        
        // 100 (new) + 0 (archived) + 0 (old BU1 excluded) = 100 <= 1000
        assertDoesNotThrow(() -> warehouseValidator.validate(newWarehouse, oldWarehouse));
    }

    @Test
    public void testValidateMaxWarehousesCalculationWithArchived() {
        Warehouse warehouse = createWarehouse("BU3", "LOC1", 100, 50);
        
        Warehouse w1 = createWarehouse("BU1", "LOC1", 100, 50);
        Warehouse wArchived = createWarehouse("BU2", "LOC1", 100, 50);
        wArchived.archivedAt = LocalDateTime.now();
        
        when(warehouseStore.findByLocation("LOC1")).thenReturn(List.of(w1, wArchived));
        
        // Only w1 is active, so active count is 1. 1 < 2 (maxNumberOfWarehouses)
        assertDoesNotThrow(() -> warehouseValidator.validate(warehouse, null));
    }

    @Test
    public void testValidateCapacityAccommodationAtLimit() {
        Warehouse oldWarehouse = createWarehouse("BU1", "LOC1", 100, 80);
        Warehouse newWarehouse = createWarehouse("BU1", "LOC1", 80, 80);
        when(warehouseStore.findByLocation("LOC1")).thenReturn(List.of(oldWarehouse));

        // capacity 80 is exactly enough for stock 80
        assertDoesNotThrow(() -> warehouseValidator.validate(newWarehouse, oldWarehouse));
    }

    @Test
    public void testValidateCapacityAccommodationSuccess() {
        Warehouse oldWarehouse = createWarehouse("BU1", "LOC1", 100, 50);
        Warehouse newWarehouse = createWarehouse("BU1", "LOC1", 150, 50);
        when(warehouseStore.findByLocation("LOC1")).thenReturn(List.of(oldWarehouse));

        // capacity 150 is more than enough for stock 50
        assertDoesNotThrow(() -> warehouseValidator.validate(newWarehouse, oldWarehouse));
    }

    @Test
    public void testValidateReplacementCapacityCheckExcludesMatchesBuCode() {
        // existingWarehouseBeingReplaced == null -> false
        // w.businessUnitCode.equals(existingWarehouseBeingReplaced.businessUnitCode) -> true
        // then the filter returns false, and the warehouse is EXCLUDED from sum.
        
        Warehouse oldWarehouse = createWarehouse("BU1", "LOC1", 100, 50);
        Warehouse newWarehouse = createWarehouse("BU1", "LOC1", 1000, 50);
        
        // oldWarehouse is BU1.
        when(warehouseStore.findByLocation("LOC1")).thenReturn(List.of(oldWarehouse));
        
        // 0 (old BU1 excluded) + 1000 (new) = 1000 <= 1000
        assertDoesNotThrow(() -> warehouseValidator.validate(newWarehouse, oldWarehouse));
    }

    @Test
    public void testValidateCapacityCalculationWhenNoReplacedWarehouse() {
        // existingWarehouseBeingReplaced == null -> true
        // then the filter returns true, and ALL warehouses are INCLUDED in sum.
        Warehouse warehouse = createWarehouse("BU_NEW", "LOC1", 100, 50);
        
        Warehouse w1 = createWarehouse("BU1", "LOC1", 900, 50);
        when(warehouseStore.findByLocation("LOC1")).thenReturn(List.of(w1));
        
        // 900 (w1 included) + 100 = 1000 <= 1000
        assertDoesNotThrow(() -> warehouseValidator.validate(warehouse, null));
    }

    @Test
    public void testValidateReplacementLocationChanged() {
        Warehouse oldWarehouse = createWarehouse("BU1", "LOC2", 100, 50);
        Warehouse newWarehouse = createWarehouse("BU1", "LOC1", 100, 50);
        when(warehouseStore.findByLocation("LOC1")).thenReturn(Collections.emptyList());

        // Location changed from LOC2 to LOC1. Should check max warehouses at LOC1.
        assertDoesNotThrow(() -> warehouseValidator.validate(newWarehouse, oldWarehouse));
    }

    @Test
    public void testValidateReplacementLocationNotChanged() {
        Warehouse oldWarehouse = createWarehouse("BU1", "LOC1", 100, 50);
        Warehouse newWarehouse = createWarehouse("BU1", "LOC1", 200, 50);
        
        // Even if max warehouses reached at LOC1, it should skip the check.
        defaultLocation.maxNumberOfWarehouses = 0; 
        when(warehouseStore.findByLocation("LOC1")).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> warehouseValidator.validate(newWarehouse, oldWarehouse));
    }

    @Test
    public void testValidateReplacementLocationIsChangingButActuallySameIdentifier() {
        Warehouse oldWarehouse = createWarehouse("BU1", "LOC1", 100, 50);
        Warehouse newWarehouse = createWarehouse("BU1", new String("LOC1"), 200, 50);
        
        // Should skip max warehouse check because identifiers are equal
        defaultLocation.maxNumberOfWarehouses = 0;
        when(warehouseStore.findByLocation("LOC1")).thenReturn(Collections.emptyList());

        assertDoesNotThrow(() -> warehouseValidator.validate(newWarehouse, oldWarehouse));
    }

    @Test
    public void testValidateReplacementWithOtherArchivedWarehouses() {
        Warehouse oldWarehouse = createWarehouse("BU1", "LOC1", 100, 50);
        Warehouse newWarehouse = createWarehouse("BU1", "LOC1", 100, 50);
        
        // An archived warehouse with the SAME BU code as the one being replaced.
        // This covers the edge case in the filter: 
        // .filter(w -> existingWarehouseBeingReplaced == null || !w.businessUnitCode.equals(existingWarehouseBeingReplaced.businessUnitCode))
        Warehouse wArchivedWithSameBU = createWarehouse("BU1", "LOC1", 500, 50);
        wArchivedWithSameBU.archivedAt = LocalDateTime.now();
        
        when(warehouseStore.findByLocation("LOC1")).thenReturn(List.of(oldWarehouse, wArchivedWithSameBU));
        
        assertDoesNotThrow(() -> warehouseValidator.validate(newWarehouse, oldWarehouse));
    }

    @Test
    public void testValidateCapacityAccommodationFailure() {
        Warehouse oldWarehouse = createWarehouse("BU1", "LOC1", 100, 80);
        Warehouse newWarehouse = createWarehouse("BU1", "LOC1", 50, 50);
        when(warehouseStore.findByLocation("LOC1")).thenReturn(List.of(oldWarehouse));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, 
            () -> warehouseValidator.validate(newWarehouse, oldWarehouse));
        assertEquals("New capacity cannot accommodate old stock", exception.getMessage());
    }

    private Warehouse createWarehouse(String buCode, String location, int capacity, int stock) {
        Warehouse w = new Warehouse();
        w.businessUnitCode = buCode;
        w.location = location;
        w.capacity = capacity;
        w.stock = stock;
        return w;
    }
}
