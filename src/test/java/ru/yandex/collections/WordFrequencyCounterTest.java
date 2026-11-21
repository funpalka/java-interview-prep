package ru.yandex.collections;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WordFrequencyCounterTest {


    @Test
    void shouldIgnoreCase() {
        Map<String, Integer> expected = Map.of("java", 3);

        Map<String, Integer> actual = WordFrequencyCounter.wordFrequency("Java java JAVA");
        //System.out.println(actual);

        assertEquals(expected, actual);
    }

    @Test
    void shouldIgnorePunctuation() {
        Map<String, Integer> expected = Map.of(
                "hello", 3,
                "world", 2);

        Map<String, Integer> actual = WordFrequencyCounter.wordFrequency("Hello, hello! World... HELLO; world?");

        assertEquals(expected, actual);
    }

    @Test
    void shouldSupportUnicodeAndDigits() {
        Map<String, Integer> expected = Map.of(
                "кот", 2,
                "java17", 2,
                "привет123", 1);

        Map<String, Integer> actual = WordFrequencyCounter.wordFrequency("кот java17 КОТ java17 привет123");

        assertEquals(expected, actual);
    }
}