package ru.yandex.streams;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LongStringCounterTest {

    @Test
    void shouldCountStringsLongerThanN() {
        List<String> strings = List.of(
                "Java", "Hi", "Spring", "Hello"
        );

        assertEquals(2L,
                LongStringCounter.countLongStrings(strings, 4));
    }

    @Test
    void shouldNotCountStringsWithLengthEqualToN() {
        List<String> strings = List.of(
                "", "Hi", "Java", "Code"
        );

        assertEquals(0L,
                LongStringCounter.countLongStrings(strings, 4));
    }

    @Test
    void shouldReturnZeroForEmptyList() {
        assertEquals(0L,
                LongStringCounter.countLongStrings(List.of(), 3));
    }
}