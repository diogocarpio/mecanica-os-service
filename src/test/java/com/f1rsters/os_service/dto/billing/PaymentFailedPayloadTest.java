package com.f1rsters.os_service.dto.billing;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class PaymentFailedPayloadTest {

    private PaymentFailedPayload payloadCompleto() {
        return new PaymentFailedPayload(
                "pay-1",
                "quote-1",
                "ERROR",
                "MERCADO_PAGO_ERROR",
                "timeout no provedor",
                Instant.parse("2026-11-16T20:05:00Z"));
    }

    @Test
    void allArgsConstructor_ShouldPopulateAllFields() {
        PaymentFailedPayload payload = payloadCompleto();

        assertEquals("pay-1", payload.getPaymentId());
        assertEquals("quote-1", payload.getQuoteId());
        assertEquals("ERROR", payload.getStatus());
        assertEquals("MERCADO_PAGO_ERROR", payload.getCode());
        assertEquals("timeout no provedor", payload.getMessage());
        assertEquals(Instant.parse("2026-11-16T20:05:00Z"), payload.getFailedAt());
    }

    @Test
    void noArgsConstructor_ShouldAllowSetters() {
        PaymentFailedPayload payload = new PaymentFailedPayload();
        payload.setPaymentId("pay-2");
        payload.setQuoteId("quote-2");
        payload.setStatus("FAILED");
        payload.setCode("CODE");
        payload.setMessage("msg");
        payload.setFailedAt(Instant.now());

        assertEquals("pay-2", payload.getPaymentId());
        assertEquals("FAILED", payload.getStatus());
    }

    @Test
    void equals_ShouldReturnTrueForEqualObjects() {
        PaymentFailedPayload a = payloadCompleto();
        PaymentFailedPayload b = payloadCompleto();

        assertEquals(a, a);
        assertEquals(a, b);
        assertEquals(b, a);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equals_ShouldReturnFalseForDifferentObjects() {
        PaymentFailedPayload base = payloadCompleto();

        assertNotEquals(base, null);
        assertNotEquals(base, "outro objeto");

        PaymentFailedPayload difPaymentId = payloadCompleto();
        difPaymentId.setPaymentId("pay-2");
        assertNotEquals(base, difPaymentId);

        PaymentFailedPayload difQuoteId = payloadCompleto();
        difQuoteId.setQuoteId("quote-2");
        assertNotEquals(base, difQuoteId);

        PaymentFailedPayload difStatus = payloadCompleto();
        difStatus.setStatus("REJECTED");
        assertNotEquals(base, difStatus);

        PaymentFailedPayload difCode = payloadCompleto();
        difCode.setCode("OTHER");
        assertNotEquals(base, difCode);

        PaymentFailedPayload difMessage = payloadCompleto();
        difMessage.setMessage("outra msg");
        assertNotEquals(base, difMessage);

        PaymentFailedPayload difFailedAt = payloadCompleto();
        difFailedAt.setFailedAt(Instant.parse("2026-11-17T00:00:00Z"));
        assertNotEquals(base, difFailedAt);

        assertNotEquals(base, new PaymentFailedPayload());
    }

    @Test
    void hashCode_ShouldBeConsistent() {
        PaymentFailedPayload a = payloadCompleto();
        PaymentFailedPayload b = payloadCompleto();

        assertEquals(a.hashCode(), a.hashCode());
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void toString_ShouldContainFieldValues() {
        String result = payloadCompleto().toString();

        assertNotNull(result);
        assertTrue(result.contains("pay-1"));
        assertTrue(result.contains("MERCADO_PAGO_ERROR"));
    }

    @Test
    void equals_ShouldHandleNullFields() {
        PaymentFailedPayload empty1 = new PaymentFailedPayload();
        PaymentFailedPayload empty2 = new PaymentFailedPayload();
        PaymentFailedPayload full = payloadCompleto();

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
        PaymentFailedPayload a = payloadCompleto();
        PaymentFailedPayload mesmoObjeto = a;
        assertTrue(a.equals(mesmoObjeto));

        PaymentFailedPayload sub = new PaymentFailedPayload() {
            @Override
            public boolean canEqual(Object o) {
                return false;
            }
        };
        assertFalse(a.equals(sub));

        PaymentFailedPayload partial1 = new PaymentFailedPayload();
        partial1.setPaymentId("A");
        PaymentFailedPayload partial2 = new PaymentFailedPayload();
        partial2.setCode("B");
        assertNotEquals(partial1, partial2);
        assertNotEquals(partial2, partial1);
    }

    @Test
    void equals_AllSubsets() {
        java.util.List<PaymentFailedPayload> objs = new java.util.ArrayList<>();
        for (int mask = 0; mask < 64; mask++) {
            PaymentFailedPayload o = new PaymentFailedPayload();
            if ((mask & 1) != 0) o.setPaymentId("P");
            if ((mask & 2) != 0) o.setQuoteId("Q");
            if ((mask & 4) != 0) o.setStatus("ERROR");
            if ((mask & 8) != 0) o.setCode("C");
            if ((mask & 16) != 0) o.setMessage("M");
            if ((mask & 32) != 0) o.setFailedAt(Instant.parse("2020-01-01T00:00:00Z"));
            objs.add(o);
        }
        for (PaymentFailedPayload a : objs) {
            for (PaymentFailedPayload b : objs) {
                a.equals(b);
            }
            a.hashCode();
            a.toString();
        }
    }
}
