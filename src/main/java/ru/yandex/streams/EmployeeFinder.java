package ru.yandex.streams;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;


public class EmployeeFinder {

    public static Optional<Employee> findFirstByCompany(
            List<Employee> employees,
            String company
    ) {

        return employees.stream()
                .filter(employee -> employee.company().equals(company))
                .findFirst();
    }


    public record Employee (String name, int age, BigDecimal salary, String company) {}
}