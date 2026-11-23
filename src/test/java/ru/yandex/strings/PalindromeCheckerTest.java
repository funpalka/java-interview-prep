package ru.yandex.strings;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PalindromeCheckerTest {

    @Test
    void shouldRecognizePalindromeIgnoreCase() {
        assertTrue(PalindromeChecker.isPalindrome("Level"));
    }

    @Test
    void shouldRecognizePalindromeIgnoreSpaces() {
        assertTrue(PalindromeChecker.isPalindrome("А роза упала на лапу Азора"));
        assertTrue(PalindromeChecker.isPalindrome("   "));
        assertTrue(PalindromeChecker.isPalindrome("b   "));
    }

    @Test
    void shouldRecognizePalindromeEmptyString() {
        assertTrue(PalindromeChecker.isPalindrome(""));
    }

    @Test
    void shouldRejectPalindromeNull() {
        assertFalse(PalindromeChecker.isPalindrome(null));
    }

    @Test
    void shouldRejectPalindrome() {
        assertFalse(PalindromeChecker.isPalindrome("null"));
        assertFalse(PalindromeChecker.isPalindrome("abcd"));
    }
}