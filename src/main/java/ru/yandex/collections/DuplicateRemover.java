package ru.yandex.collections;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class DuplicateRemover {

    /**
     * Удаление дубликатов из списка
     * Задача: реализовать метод removeDuplicates(List<Integer> list),
     * удаляющий повторяющиеся элементы с сохранением порядка их первого появления.
     */
    public static List<Integer> removeDuplicates(List<Integer> list) {
        if(list == null) {
            return new ArrayList<>();
        }
        Set<Integer> withoutDuplicates = new LinkedHashSet<>(list);
        return new ArrayList<>(withoutDuplicates);
    }
}