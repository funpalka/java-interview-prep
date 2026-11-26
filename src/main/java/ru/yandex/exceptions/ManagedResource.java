package ru.yandex.exceptions;

public class ManagedResource implements AutoCloseable {

    private final String name;

    public ManagedResource(String name) {

        this.name = name;
        System.out.println("open " + name);
    }

    @Override
    public void close() {
        System.out.println("close " + name);
    }
}