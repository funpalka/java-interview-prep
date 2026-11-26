package ru.yandex.collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class PhoneBookTest {

    private PhoneBook book;

    @BeforeEach
    void setUp() {
        book = new PhoneBook();
    }

    @Test
    void shouldAddMultipleNumbers() {
        book.addNumber("Alex", "111");
        book.addNumber("Alex", "222");

        assertEquals(
                List.of("111", "222"),
                book.getNumbers("Alex")
        );
    }

    @Test
    void shouldNotAddDuplicateNumbers() {
        book.addNumber("Alex", "111");
        book.addNumber("Alex", "111");

        assertEquals(
                List.of("111"),
                book.getNumbers("Alex")
        );
    }

    @Test
    void shouldAllowSameNumberForDifferentPeople() {
        book.addNumber("Alex", "111");
        book.addNumber("Bob", "111");

        assertEquals(List.of("111"), book.getNumbers("Alex"));
        assertEquals(List.of("111"), book.getNumbers("Bob"));
    }

    @Test
    void shouldPreserveInsertionOrder() {
        book.addNumber("Alex", "333");
        book.addNumber("Alex", "111");
        book.addNumber("Alex", "222");

        assertEquals(
                List.of("333", "111", "222"),
                book.getNumbers("Alex")
        );
    }

    @Test
    void shouldReturnIndependentList() {
        book.addNumber("Alex", "111");
        book.addNumber("Alex", "222");

        List<String> numbers = book.getNumbers("Alex");
        numbers.clear();

        assertEquals(
                List.of("111", "222"),
                book.getNumbers("Alex")
        );
    }

    @Test
    void shouldRemoveNumber() {
        book.addNumber("Alex", "111");
        book.addNumber("Alex", "222");

        book.removeNumber("Alex", "111");

        assertEquals(
                List.of("222"),
                book.getNumbers("Alex")
        );
    }

}