
package ru.yandex.exceptions;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class NonEmptyLineCounterTest {

    @TempDir
    Path tempDir;

    @Test
    void shouldCountNonEmptyLines() throws IOException {
        Path file = tempDir.resolve("test.txt");

        Files.writeString(file,
                "Hello\n" +
                        "\n" +
                        "   \n" +
                        "\t\n" +
                        "Java\n" +
                        " Stream API \n");

        assertEquals(3L, NonEmptyLineCounter.countNonEmptyLines(file));
    }

    @Test
    void shouldReturnZeroForEmptyFile() throws IOException {
        Path file = tempDir.resolve("empty.txt");

        Files.createFile(file);

        assertEquals(0L, NonEmptyLineCounter.countNonEmptyLines(file));
    }

    @Test
    void shouldThrowIOExceptionForMissingFile() {
        Path file = tempDir.resolve("missing.txt");

        assertThrows(
                IOException.class,
                () -> NonEmptyLineCounter.countNonEmptyLines(file)
        );
    }
}
