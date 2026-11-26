package ru.yandex.streams;

import java.math.BigDecimal;

public record Employee(String name, int age, BigDecimal salary, String company) {
}
