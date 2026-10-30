package com.f1rsters.os_service.dto;

import com.f1rsters.os_service.domain.StatusOrdemServico;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class StatusOrdemServicoDTOTest {

    @Test
    void from_ShouldCreateDTO() {
        Long id = 1L;
        StatusOrdemServico status = StatusOrdemServico.RECEBIDA;
        LocalDateTime criadoEm = LocalDateTime.now();

        StatusOrdemServicoDTO dto = StatusOrdemServicoDTO.from(id, status, criadoEm);

        assertEquals(id, dto.getOsId());
        assertEquals(status, dto.getStatus());
        assertEquals(criadoEm, dto.getCriadoEm());
    }
}
