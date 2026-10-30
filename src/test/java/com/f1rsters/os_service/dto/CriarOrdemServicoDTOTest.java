package com.f1rsters.os_service.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CriarOrdemServicoDTOTest {

    @Test
    void dto_ShouldAcceptFields() {
        CriarOrdemServicoDTO dto = new CriarOrdemServicoDTO();
        dto.cpfCnpjCliente = "12345678909";
        dto.placaVeiculo = "ABC1234";
        dto.servicosIds = List.of("1", "2");
        dto.pecasIds = List.of("1");
        
        assertEquals("12345678909", dto.cpfCnpjCliente);
        assertEquals("ABC1234", dto.placaVeiculo);
        assertEquals(2, dto.servicosIds.size());
        assertEquals(1, dto.pecasIds.size());
    }
}
