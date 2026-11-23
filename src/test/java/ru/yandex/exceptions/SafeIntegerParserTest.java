package ru.yandex.exceptions;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SafeIntegerParserTest {

    @Test
    void shouldParseValidIntegers() {
        assertEquals(Optional.of(123),
                SafeIntegerParser.safeParse("123"));

        assertEquals(Optional.of(-42),
                SafeIntegerParser.safeParse("-42"));

        assertEquals(Optional.of(0),
                SafeIntegerParser.safeParse("0"));
    }

    @Test
    void shouldReturnEmptyForInvalidInput() {
        assertEquals(Optional.empty(),
                SafeIntegerParser.safeParse("abc"));

        assertEquals(Optional.empty(),
                SafeIntegerParser.safeParse("12.5"));

        assertEquals(Optional.empty(),
                SafeIntegerParser.safeParse(null));

        assertEquals(Optional.empty(),
                SafeIntegerParser.safeParse("2147483648"));
    }
}