package ru.yandex.strings;

import java.util.LinkedHashMap;
import java.util.Map;

public class CharacterCounter {


    /**
     * Подсчёт частоты символов в строке
     * Задача: реализовать метод countChars(String s),
     * который подсчитывает, сколько раз каждый символ встречается в строке.
     * Возвращать Map<Character, Integer>.
     */
    public static Map<Character, Integer> countChars(String s) {
        Map<Character, Integer> result = new LinkedHashMap<>();

        if (s == null) {
            return result;
        }

        int point = 0;
        while (point < s.length()) {
            char currentChar = s.charAt(point);
            result.put(currentChar, result.getOrDefault(currentChar, 0) + 1);
            point++;
        }
        return result;
    }
}