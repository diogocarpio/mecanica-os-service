package com.f1rsters.os_service.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SensitiveDataMaskerTest {

    @Test
    void maskCpfCnpj_DeveMascararCPF() {
        String result = SensitiveDataMasker.maskCpfCnpj("12345678909");
        assertEquals("123*****909", result);
    }

    @Test
    void maskCpfCnpj_DeveMascararCNPJ() {
        String result = SensitiveDataMasker.maskCpfCnpj("12345678000195");
        assertEquals("123********195", result);
    }

    @Test
    void maskCpfCnpj_DeveMascararCPFComPontos() {
        String result = SensitiveDataMasker.maskCpfCnpj("123.456.789-09");
        assertEquals("123*****909", result);
    }

    @Test
    void maskCpfCnpj_DeveRetornarAsteriscosParaNull() {
        assertEquals("***", SensitiveDataMasker.maskCpfCnpj(null));
    }

    @Test
    void maskCpfCnpj_DeveRetornarAsteriscosParaCurto() {
        assertEquals("***", SensitiveDataMasker.maskCpfCnpj("123"));
        assertEquals("***", SensitiveDataMasker.maskCpfCnpj("1234"));
        assertEquals("***", SensitiveDataMasker.maskCpfCnpj(""));
    }

    @Test
    void maskPlaca_DeveMascararPlaca() {
        String result = SensitiveDataMasker.maskPlaca("ABC1234");
        assertEquals("AB**4", result);
    }

    @Test
    void maskPlaca_DeveRetornarAsteriscosParaCurto() {
        assertEquals("***", SensitiveDataMasker.maskPlaca("ABC"));
        assertEquals("***", SensitiveDataMasker.maskPlaca("AB"));
        assertEquals("***", SensitiveDataMasker.maskPlaca(""));
    }

    @Test
    void maskPlaca_DeveMascararPlacaMercosul() {
        String result = SensitiveDataMasker.maskPlaca("ABC1D23");
        assertEquals("AB**3", result);
    }

    @Test
    void maskPlaca_DeveRetornarAsteriscosParaNull() {
        assertEquals("***", SensitiveDataMasker.maskPlaca(null));
    }

}
