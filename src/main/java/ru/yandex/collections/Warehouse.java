package ru.yandex.collections;

import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

public class Warehouse {


    private final Map<String, Integer> warehouse = new TreeMap<>();

    public void receive(String name, int qty) {
        //wareHouse.merge(name, qty, Integer::sum);
        if (qty <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        Integer prevValue = warehouse.putIfAbsent(name, qty);
        if (prevValue != null) {
            warehouse.put(name, prevValue + qty);
        }

    }

    public boolean ship(String name, int qty) {
        if (qty <= 0) {
            return false;
        }
        Integer currentStock = warehouse.get(name);
        if (currentStock == null || currentStock < qty) {
            return false;
        }
        warehouse.put(name, currentStock - qty);
        return true;
    }

    public void printStock() {
        //System.out.println(warehouse);
        Set<Map.Entry<String, Integer>> entries = warehouse.entrySet();
        for (Map.Entry<String, Integer> entry : entries) {
            System.out.printf("%s: %d\n", entry.getKey(), entry.getValue());
        }
    }
}
