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

    @Test
    void equals_ShouldReturnTrueForEqualObjects() {
        StatusOrdemServicoDTO a = StatusOrdemServicoDTO.from(1L, StatusOrdemServico.RECEBIDA, LocalDateTime.now());
        StatusOrdemServicoDTO b = StatusOrdemServicoDTO.from(1L, StatusOrdemServico.RECEBIDA, a.getCriadoEm());

        assertEquals(a, a);
        assertEquals(a, b);
        assertEquals(b, a);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equals_ShouldReturnFalseForDifferentObjects() {
        LocalDateTime agora = LocalDateTime.now();
        StatusOrdemServicoDTO base = StatusOrdemServicoDTO.from(1L, StatusOrdemServico.RECEBIDA, agora);

        assertNotEquals(base, null);
        assertNotEquals(base, "outro objeto");

        assertNotEquals(base, StatusOrdemServicoDTO.from(2L, StatusOrdemServico.RECEBIDA, agora));
        assertNotEquals(base, StatusOrdemServicoDTO.from(1L, StatusOrdemServico.FINALIZADA, agora));
        assertNotEquals(base, StatusOrdemServicoDTO.from(1L, StatusOrdemServico.RECEBIDA, agora.plusDays(1)));
        assertNotEquals(base, new StatusOrdemServicoDTO());
    }

    @Test
    void hashCode_ShouldBeConsistent() {
        LocalDateTime agora = LocalDateTime.now();
        StatusOrdemServicoDTO a = StatusOrdemServicoDTO.from(1L, StatusOrdemServico.EM_EXECUCAO, agora);
        StatusOrdemServicoDTO b = StatusOrdemServicoDTO.from(1L, StatusOrdemServico.EM_EXECUCAO, agora);

        assertEquals(a.hashCode(), a.hashCode());
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void toString_ShouldContainFieldValues() {
        StatusOrdemServicoDTO dto = StatusOrdemServicoDTO.from(7L, StatusOrdemServico.DIAGNOSTICO, LocalDateTime.now());

        String result = dto.toString();

        assertNotNull(result);
        assertTrue(result.contains("DIAGNOSTICO"));
    }
}
