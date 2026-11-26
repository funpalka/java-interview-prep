
package ru.yandex.strings;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PhoneMaskerTest {

    @Test
    void shouldMaskPhone() {
        assertEquals("12345***8901", PhoneMasker.maskPhone("12345678901"));
    }

    @Test
    void shouldMaskPhoneWithExactlyNineCharacters() {
        assertEquals("12345***6789", PhoneMasker.maskPhone("123456789")
        );
    }

    @Test
    void shouldReturnOriginalWhenShorterThanNine() {
        assertEquals("12345678", PhoneMasker.maskPhone("12345678"));
    }

    @Test
    void shouldMaskPhoneWithPlus() {
        assertEquals("+7999***4567", PhoneMasker.maskPhone("+79991234567"));
    }

    @Test
    void shouldPreserveSpecialCharacters() {
        assertEquals("+7 (9***-456", PhoneMasker.maskPhone("+7 (999) 123-456"));
    }

    @Test
    void shouldReturnEmptyString() {
        assertEquals("", PhoneMasker.maskPhone(""));
    }

    @Test
    void shouldReturnNull() {
        assertNull(PhoneMasker.maskPhone(null));
    }
}
