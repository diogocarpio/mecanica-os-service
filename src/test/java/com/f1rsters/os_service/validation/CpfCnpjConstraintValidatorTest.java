package com.f1rsters.os_service.validation;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CpfCnpjConstraintValidatorTest {

    private final CpfCnpjConstraintValidator validator = new CpfCnpjConstraintValidator();

    @Test
    void isValid_DeveRetornarFalseParaCPFInvalido() {
        assertFalse(validator.isValid("12345678900", null));
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
