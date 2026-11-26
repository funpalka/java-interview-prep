package ru.yandex.strings;

import java.util.List;

public class StringReportBuilder {

    public static String joinWithPlus(List<String> lines) {
        String result = "";
        for (String line : lines) {
            result += line + "\n";
        }
        return result;
    }

    public static String joinWithBuilder(List<String> lines) {
        StringBuilder sb = new StringBuilder();
        for (String line : lines) {
            sb.append(line).append('\n');
        }

        return sb.toString();
    }
}