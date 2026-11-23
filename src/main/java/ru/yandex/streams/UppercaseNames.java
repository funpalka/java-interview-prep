package ru.yandex.streams;

import java.util.List;
import java.util.Locale;

public class UppercaseNames {

    public static List<String> toUppercaseNames(List<String> names) {
        //stream.toList()/Collectors.toUnmodifiableList(); неизменяемый список
        //Collectors.toList изменяемость не гарантируется
        //Collectors.toCollection(ArrayList::new) гарантированно изменяемый
        return names.stream().map(name -> name.toUpperCase(Locale.ROOT)).toList();
    }
}