package com.f1rsters.os_service.listener;

import com.f1rsters.os_service.dto.OSEvent;
import com.f1rsters.os_service.service.OrdemServicoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OrdemServicoSagaListener {
    private static final Logger log = LoggerFactory.getLogger(OrdemServicoSagaListener.class);

    private final OrdemServicoService service;

    public OrdemServicoSagaListener(OrdemServicoService service) {
        this.service = service;
    }

    @RabbitListener(queues = "billing.orcamento.aprovado.queue")
    public void onOrcamentoAprovado(OSEvent event) {
        log.info("Evento de orçamento aprovado recebido: osId={}, eventType={}", event.getOsId(), event.getEventType());
        service.processarEventoSaga(event);
    }

    @RabbitListener(queues = "billing.orcamento.rejeitado.queue")
    public void onOrcamentoRejeitado(OSEvent event) {
        log.info("Evento de orçamento rejeitado recebido: osId={}, eventType={}", event.getOsId(), event.getEventType());
        service.processarEventoSaga(event);
    }

    @RabbitListener(queues = "execution.completed.queue")
    public void onExecutionCompleted(OSEvent event) {
        log.info("Evento de execução concluída recebido: osId={}, eventType={}", event.getOsId(), event.getEventType());
        service.processarEventoSaga(event);
    }

    @RabbitListener(queues = "os.cancelled.queue")
    public void onOsCancelled(OSEvent event) {
        log.info("Evento de cancelamento da OS recebido: osId={}, eventType={}", event.getOsId(), event.getEventType());
        service.processarEventoSaga(event);
    }
}
