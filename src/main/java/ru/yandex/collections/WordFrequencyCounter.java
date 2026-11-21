package ru.yandex.collections;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class WordFrequencyCounter {

    public static Map<String, Integer> wordFrequency(String text) {

        if (text == null) {
            return new HashMap<>();
        }

        /*
        ^ - всё кроме
        L - буквы
        N - цифры
        + - последовательность
         */
        String[] words = text.split("[^\\p{L}\\p{N}]+");
        Map<String, Integer> frequency = new HashMap<>();

        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }
            String normalizedWord = word.toLowerCase(Locale.ROOT);
            frequency.put(normalizedWord, frequency.getOrDefault(normalizedWord, 0) + 1);
        }

        List<Map.Entry<String, Integer>> entries = new ArrayList<>(frequency.entrySet());

        Comparator<Map.Entry<String, Integer>> comparator =
                (a, b) -> {
                    int s = b.getValue().compareTo(a.getValue());
                    if (s != 0) {
                        return s;
                    }
                    return a.getKey().compareTo(b.getKey());

                };

        entries.sort(comparator);

        Map<String, Integer> result = new LinkedHashMap<>();
        for (int i = 0; i < (entries.size() < 3 ? entries.size() : 3); i++) {
            result.put(entries.get(i).getKey(), entries.get(i).getValue());
        }

        return result;
    }
}