package ru.yandex.streams;

import java.util.List;

public class EvenNumberSum {

    public static int sumEvenNumbers(List<Integer> numbers) {

        //return numbers.stream()
        //        .filter(num -> num % 2 == 0)
        //        .mapToInt(Integer::intValue)
        //        .sum();
        return numbers.stream()
                .filter(num -> num % 2 == 0)
                .reduce(0, Integer::sum);
    }


}