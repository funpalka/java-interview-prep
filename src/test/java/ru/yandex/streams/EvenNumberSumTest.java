package ru.yandex.streams;

import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class EvenNumberSumTest {

    @Test
    void shouldSumEvenNumbers() {
        assertEquals(
                12,
                EvenNumberSum.sumEvenNumbers(
                        List.of(1, 2, 3, 4, 6)
                )
        );
    }

    @Test
    void shouldHandleNegativeNumbersAndZero() {
        assertEquals(
                -4,
                EvenNumberSum.sumEvenNumbers(
                        List.of(-6, -3, 0, 2, 5)
                )
        );
    }

    @Test
    void shouldReturnZeroWhenNoEvenNumbers() {
        assertEquals(
                0,
                EvenNumberSum.sumEvenNumbers(
                        List.of(1, 3, 5, 7)
                )
        );
    }

    @Test
    void shouldReturnZeroForEmptyList() {
        assertEquals(
                0,
                EvenNumberSum.sumEvenNumbers(List.of())
        );
    }
}