package com.f1rsters.os_service.dto;

import com.f1rsters.os_service.domain.StatusOrdemServico;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AtualizarStatusOSDTOTest {

    @Test
    void dto_ShouldAcceptFields() {
        AtualizarStatusOSDTO dto = new AtualizarStatusOSDTO();
        dto.setNovoStatus(StatusOrdemServico.EM_EXECUCAO);

        assertEquals(StatusOrdemServico.EM_EXECUCAO, dto.getNovoStatus());
    }

    @Test
    void equals_ShouldReturnTrueForEqualObjects() {
        AtualizarStatusOSDTO a = new AtualizarStatusOSDTO();
        a.novoStatus = StatusOrdemServico.DIAGNOSTICO;
        AtualizarStatusOSDTO b = new AtualizarStatusOSDTO();
        b.novoStatus = StatusOrdemServico.DIAGNOSTICO;

        assertEquals(a, a);
        assertEquals(a, b);
        assertEquals(b, a);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equals_ShouldReturnFalseForDifferentObjects() {
        AtualizarStatusOSDTO base = new AtualizarStatusOSDTO();
        base.novoStatus = StatusOrdemServico.DIAGNOSTICO;

        assertNotEquals(base, null);
        assertNotEquals(base, "outro objeto");

        AtualizarStatusOSDTO difStatus = new AtualizarStatusOSDTO();
        difStatus.novoStatus = StatusOrdemServico.FINALIZADA;
        assertNotEquals(base, difStatus);

        AtualizarStatusOSDTO nulo = new AtualizarStatusOSDTO();
        assertNotEquals(base, nulo);
    }

    @Test
    void hashCode_ShouldBeConsistent() {
        AtualizarStatusOSDTO a = new AtualizarStatusOSDTO();
        a.novoStatus = StatusOrdemServico.CANCELADA;
        AtualizarStatusOSDTO b = new AtualizarStatusOSDTO();
        b.novoStatus = StatusOrdemServico.CANCELADA;

        assertEquals(a.hashCode(), a.hashCode());
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void toString_ShouldContainFieldValues() {
        AtualizarStatusOSDTO dto = new AtualizarStatusOSDTO();
        dto.novoStatus = StatusOrdemServico.ENTREGUE;

        String result = dto.toString();

        assertNotNull(result);
        assertTrue(result.contains("ENTREGUE"));
    }
}
