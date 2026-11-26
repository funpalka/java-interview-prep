package ru.yandex.exceptions;

import java.util.Optional;

public class SafeIntegerParser {

    public static Optional<Integer> safeParse(String input) {

        try {
            return Optional.of(Integer.parseInt(input));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }
}