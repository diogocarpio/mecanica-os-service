package com.f1rsters.os_service.dto;

import com.f1rsters.os_service.domain.StatusOrdemServico;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NotificacaoStatusDTOTest {

    @Test
    void dto_ShouldAcceptFields() {
        NotificacaoStatusDTO dto = new NotificacaoStatusDTO();
        dto.novoStatus = StatusOrdemServico.EM_EXECUCAO;
        
        assertEquals(StatusOrdemServico.EM_EXECUCAO, dto.novoStatus);
    }
}
