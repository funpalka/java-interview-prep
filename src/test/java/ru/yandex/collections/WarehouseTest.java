package ru.yandex.collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WarehouseTest {

    private Warehouse warehouse;

    @BeforeEach
    void setUp() {
        this.warehouse = new Warehouse();
    }

    @Test
    void shouldReceiveAndAccumulateStock() {
        warehouse.receive("Apple", 10);
        warehouse.receive("Apple", 5);

        assertTrue(warehouse.ship("Apple", 15));
        assertFalse(warehouse.ship("Apple", 1));
    }

    @Test
    void shouldNotShipWhenStockIsInsufficient() {
        warehouse.receive("Banana", 5);
        warehouse.receive("Apple", 10);
        warehouse.receive("Apple", 5);
        warehouse.printStock();

        assertFalse(warehouse.ship("Banana", 10));
        assertTrue(warehouse.ship("Banana", 5));
    }

    @Test
    void shouldRejectNegativeShipment () {
        warehouse.receive("Apple", 10);

        assertFalse(warehouse.ship("Apple", -5));
        assertTrue(warehouse.ship("Apple", 10));
    }

    @Test
    void shouldNotShipUnknownProduct() {
        assertFalse(warehouse.ship("Orange", 3));
    }
}
