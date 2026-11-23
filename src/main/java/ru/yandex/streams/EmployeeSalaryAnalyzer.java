package ru.yandex.streams;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class EmployeeSalaryAnalyzer {


    public static Map<String, BigDecimal> averageSalaryByCompany(List<Employee> employees) {


        //Map<String, List<Employee>> groupByCompany = employees.stream().collect(Collectors.groupingBy(Employee::company));
        //Map<String, BigDecimal> result = new HashMap<>();
        //for (Map.Entry<String, List<Employee>> stringListEntry : groupByCompany.entrySet()) {
        //    result.put(
        //            stringListEntry.getKey(),
        //            stringListEntry.getValue().stream()
        //                    .map(Employee::salary)
        //                    .reduce(BigDecimal.ZERO, BigDecimal::add)
        //                    .divide(
        //                            BigDecimal.valueOf(stringListEntry.getValue().size()),
        //                            2,
        //                            RoundingMode.HALF_UP)
        //    );
        //}

        return employees.stream()
                .collect(Collectors.groupingBy(Employee::company))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> (
                                entry.getValue().stream()
                                        .map(Employee::salary)
                                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                                        .divide(
                                                BigDecimal.valueOf(entry.getValue().size()),
                                                2,
                                                RoundingMode.HALF_UP
                                        )
                        )

                ));

    }


    public record Employee(String name, int age, BigDecimal salary, String company) {
    }
}