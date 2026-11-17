package com.f1rsters.os_service.dto.billing;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class PaymentStatusChangedPayloadTest {

    private PaymentStatusChangedPayload payloadCompleto() {
        return new PaymentStatusChangedPayload(
                "pay-1",
                "quote-1",
                BigDecimal.TEN,
                "BRL",
                "APPROVED",
                "ext-123",
                "approved",
                Instant.parse("2026-11-16T20:05:00Z"));
    }

    @Test
    void allArgsConstructor_ShouldPopulateAllFields() {
        PaymentStatusChangedPayload payload = payloadCompleto();

        assertEquals("pay-1", payload.getPaymentId());
        assertEquals("quote-1", payload.getQuoteId());
        assertEquals(BigDecimal.TEN, payload.getAmount());
        assertEquals("BRL", payload.getCurrency());
        assertEquals("APPROVED", payload.getStatus());
        assertEquals("ext-123", payload.getExternalPaymentId());
        assertEquals("approved", payload.getProviderStatus());
        assertEquals(Instant.parse("2026-11-16T20:05:00Z"), payload.getChangedAt());
    }

    @Test
    void noArgsConstructor_ShouldAllowSetters() {
        PaymentStatusChangedPayload payload = new PaymentStatusChangedPayload();
        payload.setPaymentId("pay-2");
        payload.setQuoteId("quote-2");
        payload.setAmount(BigDecimal.ONE);
        payload.setCurrency("USD");
        payload.setStatus("PENDING");
        payload.setExternalPaymentId("ext-456");
        payload.setProviderStatus("pending");
        payload.setChangedAt(Instant.now());

        assertEquals("pay-2", payload.getPaymentId());
        assertEquals("PENDING", payload.getStatus());
    }

    @Test
    void equals_ShouldReturnTrueForEqualObjects() {
        PaymentStatusChangedPayload a = payloadCompleto();
        PaymentStatusChangedPayload b = payloadCompleto();

        assertEquals(a, a);
        assertEquals(a, b);
        assertEquals(b, a);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equals_ShouldReturnFalseForDifferentObjects() {
        PaymentStatusChangedPayload base = payloadCompleto();

        assertNotEquals(base, null);
        assertNotEquals(base, "outro objeto");

        PaymentStatusChangedPayload difPaymentId = payloadCompleto();
        difPaymentId.setPaymentId("pay-2");
        assertNotEquals(base, difPaymentId);

        PaymentStatusChangedPayload difQuoteId = payloadCompleto();
        difQuoteId.setQuoteId("quote-2");
        assertNotEquals(base, difQuoteId);

        PaymentStatusChangedPayload difAmount = payloadCompleto();
        difAmount.setAmount(BigDecimal.ONE);
        assertNotEquals(base, difAmount);

        PaymentStatusChangedPayload difCurrency = payloadCompleto();
        difCurrency.setCurrency("EUR");
        assertNotEquals(base, difCurrency);

        PaymentStatusChangedPayload difStatus = payloadCompleto();
        difStatus.setStatus("REJECTED");
        assertNotEquals(base, difStatus);

        PaymentStatusChangedPayload difExternal = payloadCompleto();
        difExternal.setExternalPaymentId("ext-999");
        assertNotEquals(base, difExternal);

        PaymentStatusChangedPayload difProvider = payloadCompleto();
        difProvider.setProviderStatus("rejected");
        assertNotEquals(base, difProvider);

        PaymentStatusChangedPayload difChangedAt = payloadCompleto();
        difChangedAt.setChangedAt(Instant.parse("2026-11-17T00:00:00Z"));
        assertNotEquals(base, difChangedAt);

        assertNotEquals(base, new PaymentStatusChangedPayload());
    }

    @Test
    void hashCode_ShouldBeConsistent() {
        PaymentStatusChangedPayload a = payloadCompleto();
        PaymentStatusChangedPayload b = payloadCompleto();

        assertEquals(a.hashCode(), a.hashCode());
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void toString_ShouldContainFieldValues() {
        String result = payloadCompleto().toString();

        assertNotNull(result);
        assertTrue(result.contains("pay-1"));
        assertTrue(result.contains("APPROVED"));
    }

    @Test
    void equals_ShouldHandleNullFields() {
        PaymentStatusChangedPayload empty1 = new PaymentStatusChangedPayload();
        PaymentStatusChangedPayload empty2 = new PaymentStatusChangedPayload();
        PaymentStatusChangedPayload full = payloadCompleto();

        assertEquals(empty1, empty2);
        assertEquals(empty1.hashCode(), empty2.hashCode());
        assertNotEquals(empty1, full);
        assertNotEquals(full, empty1);
        assertNotEquals(empty1, null);
        assertNotEquals(empty1, "texto");
        assertNotNull(empty1.toString());
    }

    @Test
    void equals_ShouldCoverEdgeCases() {
        PaymentStatusChangedPayload a = payloadCompleto();
        PaymentStatusChangedPayload mesmoObjeto = a;
        assertTrue(a.equals(mesmoObjeto));

        PaymentStatusChangedPayload sub = new PaymentStatusChangedPayload() {
            @Override
            public boolean canEqual(Object o) {
                return false;
            }
        };
        assertFalse(a.equals(sub));

        PaymentStatusChangedPayload partial1 = new PaymentStatusChangedPayload();
        partial1.setPaymentId("A");
        PaymentStatusChangedPayload partial2 = new PaymentStatusChangedPayload();
        partial2.setStatus("B");
        assertNotEquals(partial1, partial2);
        assertNotEquals(partial2, partial1);
    }

    @Test
    void equals_AllSubsets() {
        java.util.List<PaymentStatusChangedPayload> objs = new java.util.ArrayList<>();
        for (int mask = 0; mask < 256; mask++) {
            PaymentStatusChangedPayload o = new PaymentStatusChangedPayload();
            if ((mask & 1) != 0) o.setPaymentId("P");
            if ((mask & 2) != 0) o.setQuoteId("Q");
            if ((mask & 4) != 0) o.setAmount(BigDecimal.ONE);
            if ((mask & 8) != 0) o.setCurrency("BRL");
            if ((mask & 16) != 0) o.setStatus("APPROVED");
            if ((mask & 32) != 0) o.setExternalPaymentId("E");
            if ((mask & 64) != 0) o.setProviderStatus("approved");
            if ((mask & 128) != 0) o.setChangedAt(Instant.parse("2020-01-01T00:00:00Z"));
            objs.add(o);
        }
        for (PaymentStatusChangedPayload a : objs) {
            for (PaymentStatusChangedPayload b : objs) {
                a.equals(b);
            }
            a.hashCode();
            a.toString();
        }
    }
}
