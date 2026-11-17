package com.f1rsters.os_service.listener;

import com.f1rsters.os_service.dto.billing.IntegrationEvent;
import com.f1rsters.os_service.dto.billing.PaymentFailedPayload;
import com.f1rsters.os_service.dto.billing.PaymentStatusChangedPayload;
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
        dispatch("billing.quote.submitted.v1", event, null);
    }

    @RabbitListener(queues = "os.billing.quote-approved.queue")
    public void onQuoteApproved(IntegrationEvent<QuoteApprovedPayload> event) {
        log.info("Evento billing.quote.approved recebido: osId={}, eventId={}", event.getOsId(), event.getEventId());
        dispatch("billing.quote.approved.v1", event, null);
    }

    @RabbitListener(queues = "os.billing.quote-rejected.queue")
    public void onQuoteRejected(IntegrationEvent<QuoteRejectedPayload> event) {
        String motivo = event.getPayload() != null ? event.getPayload().getReason() : null;
        log.info("Evento billing.quote.rejected recebido: osId={}, eventId={}, motivo={}",
                event.getOsId(), event.getEventId(), motivo);
        dispatch("billing.quote.rejected.v1", event, motivo);
    }

    @RabbitListener(queues = "os.billing.quote-failed.queue")
    public void onQuoteFailed(IntegrationEvent<QuoteFailedPayload> event) {
        String motivo = event.getPayload() != null
                ? event.getPayload().getCode() + ": " + event.getPayload().getMessage()
                : null;
        log.info("Evento billing.quote.failed recebido: osId={}, eventId={}, motivo={}",
                event.getOsId(), event.getEventId(), motivo);
        dispatch("billing.quote.failed.v1", event, motivo);
    }

    @RabbitListener(queues = "os.billing.payment-pending.queue")
    public void onPaymentPending(IntegrationEvent<PaymentStatusChangedPayload> event) {
        log.info("Evento billing.payment.pending recebido: osId={}, eventId={}", event.getOsId(), event.getEventId());
        dispatchPayment("billing.payment.pending.v1", event, null);
    }

    @RabbitListener(queues = "os.billing.payment-approved.queue")
    public void onPaymentApproved(IntegrationEvent<PaymentStatusChangedPayload> event) {
        log.info("Evento billing.payment.approved recebido: osId={}, eventId={}", event.getOsId(), event.getEventId());
        dispatchPayment("billing.payment.approved.v1", event, null);
    }

    @RabbitListener(queues = "os.billing.payment-rejected.queue")
    public void onPaymentRejected(IntegrationEvent<PaymentStatusChangedPayload> event) {
        String motivo = event.getPayload() != null ? event.getPayload().getStatus() : null;
        log.info("Evento billing.payment.rejected recebido: osId={}, eventId={}, motivo={}",
                event.getOsId(), event.getEventId(), motivo);
        dispatchPayment("billing.payment.rejected.v1", event, motivo);
    }

    @RabbitListener(queues = "os.billing.payment-failed.queue")
    public void onPaymentFailed(IntegrationEvent<PaymentFailedPayload> event) {
        String motivo = event.getPayload() != null
                ? event.getPayload().getCode() + ": " + event.getPayload().getMessage()
                : null;
        log.info("Evento billing.payment.failed recebido: osId={}, eventId={}, motivo={}",
                event.getOsId(), event.getEventId(), motivo);
        dispatchPayment("billing.payment.failed.v1", event, motivo);
    }

    private void dispatch(String eventType, IntegrationEvent<?> event, String motivo) {
        Long osId = parseOsId(event);
        if (osId == null) {
            log.warn("Evento {} ignorado: osId ausente ou inválido: {}", eventType, event.getOsId());
            return;
        }
        service.processarEventoBilling(eventType, osId, motivo);
    }

    private void dispatchPayment(String eventType, IntegrationEvent<?> event, String motivo) {
        Long osId = parseOsId(event);
        if (osId == null) {
            log.warn("Evento {} ignorado: osId ausente ou inválido: {}", eventType, event.getOsId());
            return;
        }
        service.processarEventoPagamento(eventType, osId, motivo);
    }

    private Long parseOsId(IntegrationEvent<?> event) {
        if (event.getOsId() == null || event.getOsId().isBlank()) {
            return null;
        }
        try {
            return Long.valueOf(event.getOsId());
        } catch (NumberFormatException e) {
            log.warn("osId não numérico no evento: {}", event.getOsId());
            return null;
        }
    }
}
