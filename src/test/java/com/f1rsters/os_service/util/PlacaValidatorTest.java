package com.f1rsters.os_service.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlacaValidatorTest {

    @Test
    void isValid_DeveRetornarTrueParaPlacaMercosulValida() {
        assertTrue(PlacaValidator.isValid("ABC1D23"));
        assertTrue(PlacaValidator.isValid("ABC1234"));
        assertTrue(PlacaValidator.isValid("ABC1E34"));
        assertTrue(PlacaValidator.isValid("abc1d23"));
        assertTrue(PlacaValidator.isValid("ABC-1D23"));
    }

    @Test
    void isValid_DeveRetornarTrueParaPlacaAntigaValida() {
        assertTrue(PlacaValidator.isValid("ABC1234"));
        assertTrue(PlacaValidator.isValid("abc1234"));
        assertTrue(PlacaValidator.isValid("ABC-1234"));
    }

    @Test
    void isValid_DeveRetornarFalseParaPlacaInvalida() {
        assertFalse(PlacaValidator.isValid("1234567"));
        assertFalse(PlacaValidator.isValid("AB12345"));
        assertFalse(PlacaValidator.isValid("ABCD123"));
        assertFalse(PlacaValidator.isValid("ABC12"));
        assertFalse(PlacaValidator.isValid("ABC12345"));
    }

    @Test
    void isValid_DeveRetornarFalseParaNull() {
        assertFalse(PlacaValidator.isValid(null));
    }

    @Test
    void isValid_DeveRetornarFalseParaVazio() {
        assertFalse(PlacaValidator.isValid(""));
        assertFalse(PlacaValidator.isValid("   "));
    }
}
