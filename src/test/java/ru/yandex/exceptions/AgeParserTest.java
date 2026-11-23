package ru.yandex.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AgeParserTest {

    @Test
    void shouldParseValidAge() {
        assertEquals(25, AgeParser.parseAge("25"));
    }

    @Test
    void shouldThrowValidationExceptionForInvalidInput() {
        ValidationException exception = assertThrows(
                ValidationException.class,
                () -> AgeParser.parseAge("abc")
        );

        assertEquals("Invalid age: abc", exception.getMessage());

        assertInstanceOf(
                NumberFormatException.class,
                exception.getCause()
        );
    }
}