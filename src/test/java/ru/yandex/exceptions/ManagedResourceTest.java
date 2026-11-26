package ru.yandex.exceptions;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ManagedResourceTest {

    @Test
    void shouldPrintOpenAndClose () {
        try (ManagedResource resource = new ManagedResource("text.txt")) {
            System.out.println("work");
        }

        System.out.println("finish");
    }

}