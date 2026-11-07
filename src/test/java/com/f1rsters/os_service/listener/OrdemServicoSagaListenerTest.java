package com.f1rsters.os_service.listener;

import com.f1rsters.os_service.domain.StatusOrdemServico;
import com.f1rsters.os_service.dto.OSEvent;
import com.f1rsters.os_service.service.OrdemServicoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrdemServicoSagaListenerTest {

    @Mock
    private OrdemServicoService service;

    private OrdemServicoSagaListener listener;

    @BeforeEach
    void setUp() {
        listener = new OrdemServicoSagaListener(service);
    }

    @Test
    void onOrcamentoAprovado_DeveDelegarParaService() {
        OSEvent event = evento(1L, "orcamento.aprovado");

        listener.onOrcamentoAprovado(event);

        verify(service).processarEventoSaga(event);
    }

    @Test
    void onOrcamentoRejeitado_DeveDelegarParaService() {
        OSEvent event = evento(1L, "orcamento.rejeitado");

        listener.onOrcamentoRejeitado(event);

        verify(service).processarEventoSaga(event);
    }

    @Test
    void onExecutionCompleted_DeveDelegarParaService() {
        OSEvent event = evento(1L, "execution.completed");

        listener.onExecutionCompleted(event);

        verify(service).processarEventoSaga(event);
    }

    @Test
    void onOsCancelled_DeveDelegarParaService() {
        OSEvent event = evento(1L, "os.cancelled");

        listener.onOsCancelled(event);

        verify(service).processarEventoSaga(event);
    }

    private OSEvent evento(Long osId, String eventType) {
        return new OSEvent(osId, "12345678909", "ABC1234", List.of("1"), List.of("A1"), null,
                StatusOrdemServico.RECEBIDA, eventType);
    }
}
