package ru.yandex.streams;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.stream.Collector;
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

        //return employees.stream()
        //        .collect(Collectors.groupingBy(Employee::company))
        //        .entrySet()
        //        .stream()
        //        .collect(Collectors.toMap(
        //                Map.Entry::getKey,
        //                entry -> (
        //                        entry.getValue().stream()
        //                                .map(Employee::salary)
        //                                .reduce(BigDecimal.ZERO, BigDecimal::add)
        //                                .divide(
        //                                        BigDecimal.valueOf(entry.getValue().size()),
        //                                        2,
        //                                        RoundingMode.HALF_UP
        //                                )
        //                )
        //        ));

        return employees.stream()
                .collect(Collectors.groupingBy(
                        Employee::company,
                        Collector.of(

                                //() -> new SalaryAccumulator(), //supplier - ничего не принимает возвращает объект
                                //(acc, employee) -> acc.add(employee), //BiConsumer - принимает 2 объекта ничего не возвращает
                                //(left, right) -> left.combine(right), //BinaryOperator - принимает 2 объекта вовращает 1
                                //acc -> acc.average() //Function принимает один объект возвращает результат(возможно другого типа)

                                SalaryAccumulator::new,
                                SalaryAccumulator::add,
                                SalaryAccumulator::combine,
                                SalaryAccumulator::average
                        )));

    }

    private static class SalaryAccumulator {

        private BigDecimal sum = BigDecimal.ZERO;
        private long count = 0;

        public SalaryAccumulator combine(SalaryAccumulator other) {
            this.sum = this.sum.add(other.sum);
            this.count += other.count;

            return this;
        }

        public void add(Employee employee) {
            sum = sum.add(employee.salary());
            count++;
        }

        public BigDecimal average() {
            return sum.divide(
                    BigDecimal.valueOf(count),
                    2,
                    RoundingMode.HALF_UP
            );
        }
    }
}