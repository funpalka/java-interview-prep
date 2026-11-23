package ru.yandex.strings;

import java.util.ArrayList;
import java.util.List;

public class StringReportBenchmark {

    public static void main(String[] args) {

        List<String> strings = new ArrayList<>(100_000);
        String s = ";alsjdlfkajsldkfj";
        for (int i = 0; i < 100_000; i++) {
            strings.add(s);
        }

        long startPlus = System.currentTimeMillis();
        String s1 = StringReportBuilder.joinWithPlus(strings);
        long endPlus = System.currentTimeMillis();

        long startBuilder = System.currentTimeMillis();
        String s2 = StringReportBuilder.joinWithBuilder(strings);
        long endBuilder = System.currentTimeMillis();
        System.out.printf(
                "joinWithPlusTime: %d ms, joinWithBuilderTime: %d ms%n",
                endPlus - startPlus,
                endBuilder - startBuilder
        );
        //joinWithPlusTime: 7930 ms, joinWithBuilderTime: 8 ms
    }
}