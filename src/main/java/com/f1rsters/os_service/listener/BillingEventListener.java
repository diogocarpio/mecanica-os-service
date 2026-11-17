package com.f1rsters.os_service.listener;

import com.f1rsters.os_service.dto.billing.IntegrationEvent;
import com.f1rsters.os_service.dto.billing.QuoteApprovedPayload;
import com.f1rsters.os_service.dto.billing.QuoteFailedPayload;
import com.f1rsters.os_service.dto.billing.QuoteRejectedPayload;
import com.f1rsters.os_service.dto.billing.QuoteSubmittedPayload;
import com.f1rsters.os_service.service.OrdemServicoService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class BillingEventListener {
    private static final Logger log = LoggerFactory.getLogger(BillingEventListener.class);

    private final OrdemServicoService service;

    public BillingEventListener(OrdemServicoService service) {
        this.service = service;
    }

    @RabbitListener(queues = "os.billing.quote-submitted.queue")
    public void onQuoteSubmitted(IntegrationEvent<QuoteSubmittedPayload> event) {
        log.info("Evento billing.quote.submitted recebido: osId={}, eventId={}", event.getOsId(), event.getEventId());
        service.processarEventoBilling("billing.quote.submitted.v1", parseOsId(event), null);
    }

    @RabbitListener(queues = "os.billing.quote-approved.queue")
    public void onQuoteApproved(IntegrationEvent<QuoteApprovedPayload> event) {
        log.info("Evento billing.quote.approved recebido: osId={}, eventId={}", event.getOsId(), event.getEventId());
        service.processarEventoBilling("billing.quote.approved.v1", parseOsId(event), null);
    }

    @RabbitListener(queues = "os.billing.quote-rejected.queue")
    public void onQuoteRejected(IntegrationEvent<QuoteRejectedPayload> event) {
        String motivo = event.getPayload() != null ? event.getPayload().getReason() : null;
        log.info("Evento billing.quote.rejected recebido: osId={}, eventId={}, motivo={}",
                event.getOsId(), event.getEventId(), motivo);
        service.processarEventoBilling("billing.quote.rejected.v1", parseOsId(event), motivo);
    }

    @RabbitListener(queues = "os.billing.quote-failed.queue")
    public void onQuoteFailed(IntegrationEvent<QuoteFailedPayload> event) {
        String motivo = event.getPayload() != null
                ? event.getPayload().getCode() + ": " + event.getPayload().getMessage()
                : null;
        log.info("Evento billing.quote.failed recebido: osId={}, eventId={}, motivo={}",
                event.getOsId(), event.getEventId(), motivo);
        service.processarEventoBilling("billing.quote.failed.v1", parseOsId(event), motivo);
    }

    private Long parseOsId(IntegrationEvent<?> event) {
        return event.getOsId() != null ? Long.valueOf(event.getOsId()) : null;
    }
}
