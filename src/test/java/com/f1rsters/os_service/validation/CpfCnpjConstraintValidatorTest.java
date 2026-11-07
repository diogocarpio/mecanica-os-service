package com.f1rsters.os_service.validation;

import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import static org.junit.jupiter.api.Assertions.*;

class CpfCnpjConstraintValidatorTest {

    private CpfCnpjConstraintValidator validator;

    @Mock
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new CpfCnpjConstraintValidator();
    }

    @Test
    void isValid_DeveRetornarFalseParaCPFInvalido() {
        assertFalse(validator.isValid("12345678900", context));
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
