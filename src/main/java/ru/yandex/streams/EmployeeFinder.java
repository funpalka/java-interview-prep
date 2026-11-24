package ru.yandex.streams;

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
}