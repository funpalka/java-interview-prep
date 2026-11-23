
package ru.yandex.collections;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SiteStatsTest {

    @Test
    void shouldCountUniqueVisitors() {
        SiteStats stats = new SiteStats();
        LocalDate date = LocalDate.of(2026, 11, 23);

        stats.visit(date, "Alex");
        stats.visit(date, "Bob");
        stats.visit(date, "John");

        assertEquals(3, stats.uniqueVisitors(date));
    }

    @Test
    void shouldNotCountDuplicateVisits() {
        SiteStats stats = new SiteStats();
        LocalDate date = LocalDate.of(2026, 11, 23);

        stats.visit(date, "Alex");
        stats.visit(date, "Alex");
        stats.visit(date, "Alex");

        assertEquals(1, stats.uniqueVisitors(date));
    }

    @Test
    void shouldKeepStatisticsForDifferentDates() {
        SiteStats stats = new SiteStats();

        LocalDate firstDate = LocalDate.of(2026, 11, 23);
        LocalDate secondDate = LocalDate.of(2026, 11, 24);

        stats.visit(firstDate, "Alex");
        stats.visit(firstDate, "Bob");
        stats.visit(secondDate, "Alex");

        assertEquals(2, stats.uniqueVisitors(firstDate));
        assertEquals(1, stats.uniqueVisitors(secondDate));

        assertFalse(stats.wasUserOn(secondDate, "Bob"));
    }

    @Test
    void shouldCheckIfUserVisited() {
        SiteStats stats = new SiteStats();
        LocalDate date = LocalDate.of(2026, 11, 23);

        stats.visit(date, "Alex");

        assertTrue(stats.wasUserOn(date, "Alex"));
        assertFalse(stats.wasUserOn(date, "Bob"));
    }

    @Test
    void shouldBeCaseSensitive() {
        SiteStats stats = new SiteStats();
        LocalDate date = LocalDate.of(2026, 11, 23);

        stats.visit(date, "Alex");
        stats.visit(date, "alex");

        assertEquals(2, stats.uniqueVisitors(date));
        assertTrue(stats.wasUserOn(date, "Alex"));
        assertTrue(stats.wasUserOn(date, "alex"));
        assertFalse(stats.wasUserOn(date, "ALEX"));
    }
}
