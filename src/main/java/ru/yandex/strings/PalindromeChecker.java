package ru.yandex.strings;

public class PalindromeChecker {

    public static boolean isPalindrome(String s) {
        if (s == null) {
            return false;
        }
        int length = s.length();
        int leftPoint = 0;
        int rightPoint = length - 1;

        while (leftPoint < rightPoint) {
            while (leftPoint < rightPoint && Character.isWhitespace(s.charAt(leftPoint))) {
                leftPoint++;
            }
            while (leftPoint < rightPoint && Character.isWhitespace(s.charAt(rightPoint))) {
                rightPoint--;
            }
            char left = Character.toLowerCase(s.charAt(leftPoint));
            char right = Character.toLowerCase(s.charAt(rightPoint));
            if (right != left) {
                return false;
            }
            leftPoint++;
            rightPoint--;
        }
        return true;
    }
}
