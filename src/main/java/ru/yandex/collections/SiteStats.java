package ru.yandex.collections;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class SiteStats {


    private final Map<LocalDate, Set<String>> statistics = new HashMap<>();
    // Потокобезопасно
    // private final ConcurrentHashMap<LocalDate, Set<String>> synchronizedStatistic = new ConcurrentHashMap<>();

    public void visit(LocalDate date, String userId) {
        statistics.computeIfAbsent(date, k -> new HashSet<>()).add(userId);
        // synchronizedStatistic.computeIfAbsent(date, k -> ConcurrentHashMap.newKeySet()).add(userId);
    }

    public int uniqueVisitors(LocalDate date) {
        Set<String> orDefault = statistics.get(date);
        return orDefault == null ? 0 : orDefault.size();
    }

    public boolean wasUserOn(LocalDate date, String userId) {
        Set<String> orDefault = statistics.get(date);
        return orDefault != null && orDefault.contains(userId);
    }
}
