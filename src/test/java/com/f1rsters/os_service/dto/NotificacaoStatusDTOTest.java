package com.f1rsters.os_service.dto;

import com.f1rsters.os_service.domain.StatusOrdemServico;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotificacaoStatusDTOTest {

    @Test
    void dto_ShouldAcceptFields() {
        NotificacaoStatusDTO dto = new NotificacaoStatusDTO();
        dto.setNovoStatus(StatusOrdemServico.EM_EXECUCAO);

        assertEquals(StatusOrdemServico.EM_EXECUCAO, dto.getNovoStatus());
    }

    @Test
    void equals_ShouldReturnTrueForEqualObjects() {
        NotificacaoStatusDTO a = new NotificacaoStatusDTO();
        a.novoStatus = StatusOrdemServico.DIAGNOSTICO;
        NotificacaoStatusDTO b = new NotificacaoStatusDTO();
        b.novoStatus = StatusOrdemServico.DIAGNOSTICO;

        assertEquals(a, a);
        assertEquals(a, b);
        assertEquals(b, a);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equals_ShouldReturnFalseForDifferentObjects() {
        NotificacaoStatusDTO base = new NotificacaoStatusDTO();
        base.novoStatus = StatusOrdemServico.DIAGNOSTICO;

        assertNotEquals(base, null);
        assertNotEquals(base, "outro objeto");

        NotificacaoStatusDTO difStatus = new NotificacaoStatusDTO();
        difStatus.novoStatus = StatusOrdemServico.CANCELADA;
        assertNotEquals(base, difStatus);

        assertNotEquals(base, new NotificacaoStatusDTO());
    }

    @Test
    void hashCode_ShouldBeConsistent() {
        NotificacaoStatusDTO a = new NotificacaoStatusDTO();
        a.novoStatus = StatusOrdemServico.FINALIZADA;
        NotificacaoStatusDTO b = new NotificacaoStatusDTO();
        b.novoStatus = StatusOrdemServico.FINALIZADA;

        assertEquals(a.hashCode(), a.hashCode());
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void toString_ShouldContainFieldValues() {
        NotificacaoStatusDTO dto = new NotificacaoStatusDTO();
        dto.novoStatus = StatusOrdemServico.AGUARDANDO_APROVACAO;

        String result = dto.toString();

        assertNotNull(result);
        assertTrue(result.contains("AGUARDANDO_APROVACAO"));
    }
}
