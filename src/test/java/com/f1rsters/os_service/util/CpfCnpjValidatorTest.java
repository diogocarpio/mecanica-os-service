package com.f1rsters.os_service.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CpfCnpjValidatorTest {

    @Test
    void isValid_DeveRetornarTrueParaCPFValido() {
        assertTrue(CpfCnpjValidator.isValid("100.000.000-00"));
        assertTrue(CpfCnpjValidator.isValid("10000000000"));
    }

    @Test
    void isValid_DeveRetornarTrueParaCNPJValido() {
        assertTrue(CpfCnpjValidator.isValid("01.000.000/0000-73"));
        assertTrue(CpfCnpjValidator.isValid("01000000000073"));
    }

    @Test
    void isValid_DeveRetornarFalseParaCPFInvalido() {
        assertFalse(CpfCnpjValidator.isValid("123.456.789-00"));
        assertFalse(CpfCnpjValidator.isValid("111.111.111-11"));
        assertFalse(CpfCnpjValidator.isValid("12345678900"));
    }

    @Test
    void isValid_DeveRetornarFalseParaCNPJInvalido() {
        assertFalse(CpfCnpjValidator.isValid("12.345.678/0001-00"));
        assertFalse(CpfCnpjValidator.isValid("11.111.111/1111-11"));
        assertFalse(CpfCnpjValidator.isValid("12345678000100"));
    }

    @Test
    void isValid_DeveRetornarFalseParaNull() {
        assertFalse(CpfCnpjValidator.isValid(null));
    }

    @Test
    void isValid_DeveRetornarFalseParaVazio() {
        assertFalse(CpfCnpjValidator.isValid(""));
        assertFalse(CpfCnpjValidator.isValid("   "));
    }

    @Test
    void isValid_DeveRetornarFalseParaTamanhoInvalido() {
        assertFalse(CpfCnpjValidator.isValid("123456789"));
        assertFalse(CpfCnpjValidator.isValid("123456789012345"));
    }
}
