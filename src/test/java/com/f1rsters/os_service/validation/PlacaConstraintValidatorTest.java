package com.f1rsters.os_service.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PlacaConstraintValidatorTest {

    private final PlacaConstraintValidator validator = new PlacaConstraintValidator();

    @Test
    void isValid_DeveRetornarTrueParaPlacaValida() {
        assertTrue(validator.isValid("ABC1234", null));
        assertTrue(validator.isValid("ABC1D23", null));
    }

    @Test
    void isValid_DeveRetornarFalseParaPlacaInvalida() {
        assertFalse(validator.isValid("1234567", null));
        assertFalse(validator.isValid("AB12345", null));
    }

    @Test
    void isValid_DeveRetornarTrueParaNull() {
        assertTrue(validator.isValid(null, null));
    }

    @Test
    void isValid_DeveRetornarTrueParaVazio() {
        assertTrue(validator.isValid("", null));
        assertTrue(validator.isValid("   ", null));
    }
}
