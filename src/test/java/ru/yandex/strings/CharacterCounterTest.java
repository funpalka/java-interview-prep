package ru.yandex.strings;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
class CharacterCounterTest {


    @Test
    void shouldCountCharacters() {

        Map<Character, Integer> result =
                CharacterCounter.countChars("hello");

        assertEquals(1, result.get('h'));
        assertEquals(1, result.get('e'));
        assertEquals(2, result.get('l'));
        assertEquals(1, result.get('o'));

        assertEquals(4, result.size());
    }

    @Test
    void shouldReturnEmptyMap() {

        Map<Character, Integer> resultEmpty =
                CharacterCounter.countChars("");
        Map<Character, Integer> resultNull =
                CharacterCounter.countChars(null);
        Map<Character, Integer> expected = new HashMap<>();

        assertEquals(expected, resultEmpty);
        assertEquals(expected, resultNull);
    }

    @Test
    void shouldPreserveCharacterInsertionOrder() {
        Map<Character, Integer> result =
                CharacterCounter.countChars("baca");

        List<Character> actual = new ArrayList<>(result.keySet());

        assertEquals(List.of('b', 'a', 'c'), actual);
    }
  
}