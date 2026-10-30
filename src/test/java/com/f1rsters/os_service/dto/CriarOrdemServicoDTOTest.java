package com.f1rsters.os_service.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CriarOrdemServicoDTOTest {

    @Test
    void dto_ShouldAcceptFields() {
        CriarOrdemServicoDTO dto = new CriarOrdemServicoDTO();
        dto.setCpfCnpjCliente("12345678909");
        dto.setPlacaVeiculo("ABC1234");
        dto.setServicosIds(List.of("1", "2"));
        dto.setPecasIds(List.of("1"));

        assertEquals("12345678909", dto.getCpfCnpjCliente());
        assertEquals("ABC1234", dto.getPlacaVeiculo());
        assertEquals(2, dto.getServicosIds().size());
        assertEquals(1, dto.getPecasIds().size());
    }
}
