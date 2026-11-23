package ru.yandex.streams;

import java.util.List;

public class LongStringCounter {

    public static long countLongStrings(List<String> strings, int n) {

        return strings.stream().filter(string -> string.length() > n).count();
    }
}