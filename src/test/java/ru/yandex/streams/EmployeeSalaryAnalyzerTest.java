
package ru.yandex.streams;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EmployeeSalaryAnalyzerTest {

    @Test
    void shouldCalculateAverageSalaryForEachCompany() {
        List<EmployeeSalaryAnalyzer.Employee> employees = List.of(
                new EmployeeSalaryAnalyzer.Employee(
                        "Alex", 25, new BigDecimal("100.00"), "Google"),
                new EmployeeSalaryAnalyzer.Employee(
                        "Bob", 30, new BigDecimal("200.00"), "Google"),
                new EmployeeSalaryAnalyzer.Employee(
                        "John", 28, new BigDecimal("150.00"), "Amazon"),
                new EmployeeSalaryAnalyzer.Employee(
                        "Kate", 32, new BigDecimal("200.00"), "Amazon")
        );

        Map<String, BigDecimal> result =
                EmployeeSalaryAnalyzer.averageSalaryByCompany(employees);

        assertEquals(2, result.size());
        assertEquals(new BigDecimal("150.00"), result.get("Google"));
        assertEquals(new BigDecimal("175.00"), result.get("Amazon"));
    }

    @Test
    void shouldRoundAverageSalaryHalfUp() {
        List<EmployeeSalaryAnalyzer.Employee> employees = List.of(
                new EmployeeSalaryAnalyzer.Employee(
                        "Alex", 25, new BigDecimal("100.00"), "Google"),
                new EmployeeSalaryAnalyzer.Employee(
                        "Bob", 30, new BigDecimal("100.01"), "Google")
        );

        Map<String, BigDecimal> result =
                EmployeeSalaryAnalyzer.averageSalaryByCompany(employees);

        assertEquals(new BigDecimal("100.01"), result.get("Google"));
    }

    @Test
    void shouldReturnEmptyMapForEmptyList() {
        Map<String, BigDecimal> result =
                EmployeeSalaryAnalyzer.averageSalaryByCompany(List.of());

        assertEquals(Map.of(), result);
    }
}
