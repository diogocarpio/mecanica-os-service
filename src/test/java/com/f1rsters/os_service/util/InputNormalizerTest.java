package com.f1rsters.os_service.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InputNormalizerTest {

    @Test
    void normalizeCpfCnpj_DeveRemoverCaracteresEspeciais() {
        assertEquals("12345678909", InputNormalizer.normalizeCpfCnpj("123.456.789-09"));
        assertEquals("12345678000195", InputNormalizer.normalizeCpfCnpj("12.345.678/0001-95"));
        assertEquals("12345678909", InputNormalizer.normalizeCpfCnpj("123-456-789-09"));
    }

    @Test
    void normalizeCpfCnpj_DeveRetornarNullParaNull() {
        assertNull(InputNormalizer.normalizeCpfCnpj(null));
    }

    @Test
    void normalizePlaca_DeveNormalizarParaMaiusculasESemHifen() {
        assertEquals("ABC1234", InputNormalizer.normalizePlaca("abc-1234"));
        assertEquals("ABC1D23", InputNormalizer.normalizePlaca("abc-1d23"));
        assertEquals("ABC1234", InputNormalizer.normalizePlaca("ABC1234"));
    }

    @Test
    void normalizePlaca_DeveRetornarNullParaNull() {
        assertNull(InputNormalizer.normalizePlaca(null));
    }
}
