package ru.yandex.collections;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

public class PhoneBook {

    private final Map<String, LinkedHashSet<String>> phoneBook = new HashMap<>();

    public void addNumber(String name, String number) {
        phoneBook.computeIfAbsent(name, k -> new LinkedHashSet<>()).add(number);
    }

    public List<String> getNumbers(String name) {
        return phoneBook.containsKey(name) ? new ArrayList<>(phoneBook.get(name)) : new ArrayList<>();
    }

    public void removeNumber(String name, String number) {
        LinkedHashSet<String> numbers = phoneBook.get(name);
        if (numbers != null) {
            numbers.remove(number);
            if (numbers.isEmpty()) {
                phoneBook.remove(name);
            }
        }
    }
}