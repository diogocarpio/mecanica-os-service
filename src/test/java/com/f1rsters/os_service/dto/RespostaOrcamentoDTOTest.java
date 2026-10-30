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
}
