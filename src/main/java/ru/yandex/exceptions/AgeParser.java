package ru.yandex.exceptions;

public class AgeParser {

    public static int parseAge(String input) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            throw new ValidationException("Invalid age: " + input, e);
        }
    }
}