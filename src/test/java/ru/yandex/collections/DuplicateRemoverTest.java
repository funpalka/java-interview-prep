package ru.yandex.collections;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;


class DuplicateRemoverTest {

    @Test
    void shouldRemoveDuplicates() {
        List<Integer> inputDuplicates = List.of(1,2,3,2,3,4,5,3,4,5);
        List<Integer> expectedInputDuplicates = List.of(1,2,3,4,5);

        assertEquals(expectedInputDuplicates, DuplicateRemover.removeDuplicates(inputDuplicates));
    }

    @Test
    void shouldPreserveOrder() {
        List<Integer> inputWithoutDuplicates = List.of(1,2,3,4,5);
        List<Integer> expectedInputDuplicates = List.of(1,2,3,4,5);

        assertEquals(expectedInputDuplicates, DuplicateRemover.removeDuplicates(inputWithoutDuplicates));
    }

    @Test
    void shouldHandleNullElements() {
        List<Integer> inputWithNull = new ArrayList<>();
        inputWithNull.add(null);
        inputWithNull.add(1);
        inputWithNull.add(1);

        List<Integer> expectedWithNull = new ArrayList<>();
        expectedWithNull.add(null);
        expectedWithNull.add(1);

        assertEquals(expectedWithNull, DuplicateRemover.removeDuplicates(inputWithNull));
    }

    @Test
    void shouldReturnEmptyList() {
        List<Integer> expected = new ArrayList<>();
        assertEquals(expected, DuplicateRemover.removeDuplicates(new ArrayList<>()));
        assertEquals(expected, DuplicateRemover.removeDuplicates(null));
    }
  
}