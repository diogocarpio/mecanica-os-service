package com.f1rsters.os_service.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;

class PlacaConstraintValidatorTest {

    private PlacaConstraintValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new PlacaConstraintValidator();
    }

    @Test
    void isValid_DeveRetornarTrueParaPlacaValida() {
        assertTrue(validator.isValid("ABC1234", context));
        assertTrue(validator.isValid("ABC1D23", context));
    }

    @Test
    void isValid_DeveRetornarFalseParaPlacaInvalida() {
        assertFalse(validator.isValid("1234567", context));
        assertFalse(validator.isValid("AB12345", context));
    }

    @Test
    void isValid_DeveRetornarTrueParaNull() {
        assertTrue(validator.isValid(null, context));
    }

    @Test
    void isValid_DeveRetornarTrueParaVazio() {
        assertTrue(validator.isValid("", context));
        assertTrue(validator.isValid("   ", context));
    }
}
