package ru.yandex.collections;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class SortedListMergerTest {

    @Test
    void shouldMergeSortedListsWithDuplicates() {
        List<Integer> a = List.of(1, 2, 3, 4);
        List<Integer> b = List.of(1, 1, 1, 2, 3, 4);

        List<Integer> expected = List.of(
                1, 1, 1, 1, 2, 2, 3, 3, 4, 4
        );

        List<Integer> actual = SortedListMerger.mergeSorted(a, b);

        assertEquals(expected, actual);
    }

    @Test
    void shouldMergeListsOfDifferentSizes() {
        List<Integer> a = List.of(1, 2, 3, 4);
        List<Integer> b = List.of(1, 1, 1, 23, 33, 44);

        List<Integer> expected = List.of(
                1, 1, 1, 1, 2, 3, 4, 23, 33, 44
        );

        List<Integer> actual = SortedListMerger.mergeSorted(a, b);

        assertEquals(expected, actual);
    }


    @Test
    void shouldMergeWhenFirstListIsEmpty() {
        List<Integer> a = List.of();
        List<Integer> b = List.of(1, 1, 1, 23, 33, 44);

        List<Integer> actual = SortedListMerger.mergeSorted(a, b);

        assertEquals(b, actual);
    }
}