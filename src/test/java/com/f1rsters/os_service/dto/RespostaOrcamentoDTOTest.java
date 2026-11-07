package com.f1rsters.os_service.dto;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class RespostaOrcamentoDTOTest {

    @Test
    void dto_ShouldAcceptFields() {
        RespostaOrcamentoDTO dto = new RespostaOrcamentoDTO();
        dto.setAprovado(true);

        assertTrue(dto.isAprovado());
    }

    @Test
    void equals_ShouldReturnTrueForEqualObjects() {
        RespostaOrcamentoDTO a = new RespostaOrcamentoDTO();
        a.aprovado = true;
        RespostaOrcamentoDTO b = new RespostaOrcamentoDTO();
        b.aprovado = true;

        assertEquals(a, a);
        assertEquals(a, b);
        assertEquals(b, a);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equals_ShouldReturnFalseForDifferentObjects() {
        RespostaOrcamentoDTO base = new RespostaOrcamentoDTO();
        base.aprovado = true;

        assertNotEquals(base, null);
        assertNotEquals(base, "outro objeto");

        RespostaOrcamentoDTO difAprovado = new RespostaOrcamentoDTO();
        difAprovado.aprovado = false;
        assertNotEquals(base, difAprovado);

        assertNotEquals(base, new RespostaOrcamentoDTO());
    }

    @Test
    void hashCode_ShouldBeConsistent() {
        RespostaOrcamentoDTO a = new RespostaOrcamentoDTO();
        a.aprovado = false;
        RespostaOrcamentoDTO b = new RespostaOrcamentoDTO();
        b.aprovado = false;

        assertEquals(a.hashCode(), a.hashCode());
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void toString_ShouldContainFieldValues() {
        RespostaOrcamentoDTO dto = new RespostaOrcamentoDTO();
        dto.aprovado = true;

        String result = dto.toString();

        assertNotNull(result);
        assertTrue(result.contains("aprovado"));
    }
}
