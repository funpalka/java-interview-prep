package ru.yandex.collections;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SortedListMerger {

    public static List<Integer> mergeSorted(List<Integer> a, List<Integer> b) {
        if (a == null) {
            a = List.of();
        }

        if (b == null) {
            b = List.of();
        }

        Iterator<Integer> iteratorA = a.iterator();
        Iterator<Integer> iteratorB = b.iterator();

        List<Integer> result = new ArrayList<>(a.size() + b.size());

        Integer currentA = iteratorA.hasNext()
                ? iteratorA.next()
                : null;

        Integer currentB = iteratorB.hasNext()
                ? iteratorB.next()
                : null;

        while (currentA != null || currentB != null) {
            if (currentA != null && currentB != null) {
                if (currentA <= currentB) {
                    result.add(currentA);
                    currentA = iteratorA.hasNext()
                            ? iteratorA.next()
                            : null;
                } else {
                    result.add(currentB);
                    currentB = iteratorB.hasNext()
                            ? iteratorB.next()
                            : null;
                }
            } else if (currentA != null) {
                result.add(currentA);
                currentA = iteratorA.hasNext()
                        ? iteratorA.next()
                        : null;
            } else {
                result.add(currentB);
                currentB = iteratorB.hasNext()
                        ? iteratorB.next()
                        : null;
            }
        }

        return result;
    }
}