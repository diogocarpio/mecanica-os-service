package com.f1rsters.os_service.listener;

import com.f1rsters.os_service.dto.billing.IntegrationEvent;
import com.f1rsters.os_service.dto.billing.PaymentFailedPayload;
import com.f1rsters.os_service.dto.billing.PaymentStatusChangedPayload;
import com.f1rsters.os_service.dto.billing.QuoteApprovedPayload;
import com.f1rsters.os_service.dto.billing.QuoteFailedPayload;
import com.f1rsters.os_service.dto.billing.QuoteRejectedPayload;
import com.f1rsters.os_service.dto.billing.QuoteSubmittedPayload;
import com.f1rsters.os_service.service.OrdemServicoService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class BillingEventListenerTest {

    @Mock
    private OrdemServicoService service;

    private BillingEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new BillingEventListener(service);
    }

    @Test
    void onQuoteSubmitted_DeveDelegarParaService() {
        IntegrationEvent<QuoteSubmittedPayload> event = evento("1", new QuoteSubmittedPayload());

        listener.onQuoteSubmitted(event);

        verify(service).processarEventoBilling("billing.quote.submitted.v1", 1L, null);
    }

    @Test
    void onQuoteApproved_DeveDelegarParaService() {
        IntegrationEvent<QuoteApprovedPayload> event = evento("1", new QuoteApprovedPayload());

        listener.onQuoteApproved(event);

        verify(service).processarEventoBilling("billing.quote.approved.v1", 1L, null);
    }

    @Test
    void onQuoteRejected_DeveDelegarMotivoParaService() {
        QuoteRejectedPayload payload = new QuoteRejectedPayload();
        payload.setReason("preço alto");
        IntegrationEvent<QuoteRejectedPayload> event = evento("1", payload);

        listener.onQuoteRejected(event);

        verify(service).processarEventoBilling("billing.quote.rejected.v1", 1L, "preço alto");
    }

    @Test
    void onQuoteRejected_DeveDelegarNuloQuandoPayloadNulo() {
        IntegrationEvent<QuoteRejectedPayload> event = evento("1", null);

        listener.onQuoteRejected(event);

        verify(service).processarEventoBilling("billing.quote.rejected.v1", 1L, null);
    }

    @Test
    void onQuoteFailed_DeveDelegarMotivoParaService() {
        QuoteFailedPayload payload = new QuoteFailedPayload();
        payload.setCode("INVALID_QUOTE_DATA");
        payload.setMessage("item invalido");
        IntegrationEvent<QuoteFailedPayload> event = evento("1", payload);

        listener.onQuoteFailed(event);

        verify(service).processarEventoBilling("billing.quote.failed.v1", 1L, "INVALID_QUOTE_DATA: item invalido");
    }

    @Test
    void onQuoteFailed_DeveDelegarNuloQuandoPayloadNulo() {
        IntegrationEvent<QuoteFailedPayload> event = evento("1", null);

        listener.onQuoteFailed(event);

        verify(service).processarEventoBilling("billing.quote.failed.v1", 1L, null);
    }

    @Test
    void onQuoteSubmitted_NaoDeveChamarServiceQuandoOsIdAusente() {
        IntegrationEvent<QuoteSubmittedPayload> event = evento(null, new QuoteSubmittedPayload());

        listener.onQuoteSubmitted(event);

        verify(service, never()).processarEventoBilling(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void onQuoteSubmitted_NaoDeveChamarServiceQuandoOsIdEmBranco() {
        IntegrationEvent<QuoteSubmittedPayload> event = evento("  ", new QuoteSubmittedPayload());

        listener.onQuoteSubmitted(event);

        verify(service, never()).processarEventoBilling(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void onQuoteSubmitted_NaoDeveChamarServiceQuandoOsIdNaoNumerico() {
        IntegrationEvent<QuoteSubmittedPayload> event = evento("abc", new QuoteSubmittedPayload());

        listener.onQuoteSubmitted(event);

        verify(service, never()).processarEventoBilling(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    @Test
    void onPaymentPending_DeveDelegarParaService() {
        IntegrationEvent<PaymentStatusChangedPayload> event = evento("1", new PaymentStatusChangedPayload());

        listener.onPaymentPending(event);

        verify(service).processarEventoPagamento("billing.payment.pending.v1", 1L, null);
    }

    @Test
    void onPaymentApproved_DeveDelegarParaService() {
        IntegrationEvent<PaymentStatusChangedPayload> event = evento("1", new PaymentStatusChangedPayload());

        listener.onPaymentApproved(event);

        verify(service).processarEventoPagamento("billing.payment.approved.v1", 1L, null);
    }

    @Test
    void onPaymentRejected_DeveDelegarMotivoParaService() {
        PaymentStatusChangedPayload payload = new PaymentStatusChangedPayload();
        payload.setStatus("REJECTED");
        IntegrationEvent<PaymentStatusChangedPayload> event = evento("1", payload);

        listener.onPaymentRejected(event);

        verify(service).processarEventoPagamento("billing.payment.rejected.v1", 1L, "REJECTED");
    }

    @Test
    void onPaymentRejected_DeveDelegarNuloQuandoPayloadNulo() {
        IntegrationEvent<PaymentStatusChangedPayload> event = evento("1", null);

        listener.onPaymentRejected(event);

        verify(service).processarEventoPagamento("billing.payment.rejected.v1", 1L, null);
    }

    @Test
    void onPaymentFailed_DeveDelegarMotivoParaService() {
        PaymentFailedPayload payload = new PaymentFailedPayload();
        payload.setCode("MERCADO_PAGO_ERROR");
        payload.setMessage("timeout no provedor");
        IntegrationEvent<PaymentFailedPayload> event = evento("1", payload);

        listener.onPaymentFailed(event);

        verify(service).processarEventoPagamento("billing.payment.failed.v1", 1L, "MERCADO_PAGO_ERROR: timeout no provedor");
    }

    @Test
    void onPaymentFailed_DeveDelegarNuloQuandoPayloadNulo() {
        IntegrationEvent<PaymentFailedPayload> event = evento("1", null);

        listener.onPaymentFailed(event);

        verify(service).processarEventoPagamento("billing.payment.failed.v1", 1L, null);
    }

    @Test
    void onPaymentApproved_NaoDeveChamarServiceQuandoOsIdInvalido() {
        IntegrationEvent<PaymentStatusChangedPayload> event = evento("abc", new PaymentStatusChangedPayload());

        listener.onPaymentApproved(event);

        verify(service, never()).processarEventoPagamento(org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any());
    }

    private <T> IntegrationEvent<T> evento(String osId, T payload) {
        IntegrationEvent<T> event = new IntegrationEvent<>();
        event.setEventId(UUID.randomUUID());
        event.setEventType("test.event");
        event.setEventVersion(1);
        event.setOccurredAt(Instant.now());
        event.setSource("billing-service");
        event.setCorrelationId("corr-1");
        event.setOsId(osId);
        event.setPayload(payload);
        return event;
    }
}
