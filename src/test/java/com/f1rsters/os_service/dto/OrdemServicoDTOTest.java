package com.f1rsters.os_service.dto;

import com.f1rsters.os_service.domain.StatusOrdemServico;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrdemServicoDTOTest {

    @Test
    void dto_ShouldAcceptFields() {
        OrdemServicoDTO dto = new OrdemServicoDTO();
        dto.setId(1L);
        dto.setClienteId("12345678909");
        dto.setVeiculoId("ABC1234");
        dto.setServicosIds(List.of("S1", "S2"));
        dto.setPecasIds(List.of("P1"));
        dto.setStatus(StatusOrdemServico.RECEBIDA);
        dto.setValorTotal(BigDecimal.valueOf(1000.0));
        dto.setCriadoEm(LocalDateTime.now());
        dto.setAtualizadoEm(LocalDateTime.now());

        assertEquals(1L, dto.getId());
        assertEquals("12345678909", dto.getClienteId());
        assertEquals("ABC1234", dto.getVeiculoId());
        assertEquals(StatusOrdemServico.RECEBIDA, dto.getStatus());
        assertEquals(BigDecimal.valueOf(1000.0), dto.getValorTotal());
        assertEquals(List.of("S1", "S2"), dto.getServicosIds());
        assertEquals(List.of("P1"), dto.getPecasIds());
        assertNotNull(dto.getCriadoEm());
        assertNotNull(dto.getAtualizadoEm());
    }
}
