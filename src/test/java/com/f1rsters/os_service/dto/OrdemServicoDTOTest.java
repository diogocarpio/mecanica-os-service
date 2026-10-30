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
        dto.id = 1L;
        dto.clienteId = "12345678909";
        dto.veiculoId = "ABC1234";
        dto.status = StatusOrdemServico.RECEBIDA;
        dto.valorTotal = BigDecimal.valueOf(1000.0);
        dto.criadoEm = LocalDateTime.now();
        
        assertEquals(1L, dto.id);
        assertEquals(StatusOrdemServico.RECEBIDA, dto.status);
    }
}
