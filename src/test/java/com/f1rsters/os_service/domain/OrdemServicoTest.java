package com.f1rsters.os_service.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrdemServicoTest {

    @Test
    void settersAndGetters_ShouldWork() {
        OrdemServico os = new OrdemServico();
        os.setId(1L);
        os.setClienteId("12345678909");
        os.setVeiculoId("ABC1234");
        os.setStatus(StatusOrdemServico.RECEBIDA);
        os.setValorTotal(BigDecimal.valueOf(1000.0));
        os.setCriadoEm(LocalDateTime.now());
        os.setAtualizadoEm(LocalDateTime.now());
        
        assertEquals(1L, os.getId());
        assertEquals("12345678909", os.getClienteId());
        assertEquals("ABC1234", os.getVeiculoId());
        assertEquals(StatusOrdemServico.RECEBIDA, os.getStatus());
        assertEquals(BigDecimal.valueOf(1000.0), os.getValorTotal());
        assertNotNull(os.getCriadoEm());
        assertNotNull(os.getAtualizadoEm());
    }
}
