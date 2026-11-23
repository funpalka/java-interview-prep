package ru.yandex.streams;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class UppercaseNamesTest {
    @Test
    void shouldConvertNamesToUppercase() {
        List<String> names = List.of("Alex", "bob", "Иван");

        assertEquals(
                List.of("ALEX", "BOB", "ИВАН"),
                UppercaseNames.toUppercaseNames(names)
        );
    }

    @Test
    void shouldPreserveDuplicatesAndOrder() {
        List<String> names = List.of("alex", "bob", "alex");

        assertEquals(
                List.of("ALEX", "BOB", "ALEX"),
                UppercaseNames.toUppercaseNames(names)
        );
    }

    @Test
    void shouldReturnEmptyList() {
        assertEquals(
                List.of(),
                UppercaseNames.toUppercaseNames(List.of())
        );
    }

}