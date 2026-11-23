package ru.yandex.exceptions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

public class NonEmptyLineCounter {

    public static long countNonEmptyLines(Path path) throws IOException {
        try (Stream<String> lines = Files.lines(path)) {
            return lines.filter(line -> !line.isBlank()).count();
        }
    }
}