package com.f1rsters.os_service.dto.billing;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class QuoteSubmittedPayloadTest {

    private QuoteSubmittedPayload payloadCompleto() {
        return new QuoteSubmittedPayload(
                "quote-1",
                BigDecimal.ZERO,
                "BRL",
                "PENDING",
                Instant.parse("2026-11-23T20:00:00Z"));
    }

    @Test
    void allArgsConstructor_ShouldPopulateAllFields() {
        QuoteSubmittedPayload payload = payloadCompleto();

        assertEquals("quote-1", payload.getQuoteId());
        assertEquals(BigDecimal.ZERO, payload.getTotal());
        assertEquals("BRL", payload.getCurrency());
        assertEquals("PENDING", payload.getStatus());
        assertEquals(Instant.parse("2026-11-23T20:00:00Z"), payload.getValidUntil());
    }

    @Test
    void noArgsConstructor_ShouldAllowSetters() {
        QuoteSubmittedPayload payload = new QuoteSubmittedPayload();
        payload.setQuoteId("quote-2");
        payload.setTotal(BigDecimal.TEN);
        payload.setCurrency("USD");
        payload.setStatus("APPROVED");
        payload.setValidUntil(Instant.now());

        assertEquals("quote-2", payload.getQuoteId());
        assertEquals("APPROVED", payload.getStatus());
    }

    @Test
    void equals_ShouldReturnTrueForEqualObjects() {
        QuoteSubmittedPayload a = payloadCompleto();
        QuoteSubmittedPayload b = payloadCompleto();

        assertEquals(a, a);
        assertEquals(a, b);
        assertEquals(b, a);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equals_ShouldReturnFalseForDifferentObjects() {
        QuoteSubmittedPayload base = payloadCompleto();

        assertNotEquals(base, null);
        assertNotEquals(base, "outro objeto");

        QuoteSubmittedPayload difQuoteId = payloadCompleto();
        difQuoteId.setQuoteId("quote-2");
        assertNotEquals(base, difQuoteId);

        QuoteSubmittedPayload difTotal = payloadCompleto();
        difTotal.setTotal(BigDecimal.ONE);
        assertNotEquals(base, difTotal);

        QuoteSubmittedPayload difCurrency = payloadCompleto();
        difCurrency.setCurrency("EUR");
        assertNotEquals(base, difCurrency);

        QuoteSubmittedPayload difStatus = payloadCompleto();
        difStatus.setStatus("REJECTED");
        assertNotEquals(base, difStatus);

        assertNotEquals(base, new QuoteSubmittedPayload());
    }

    @Test
    void hashCode_ShouldBeConsistent() {
        QuoteSubmittedPayload a = payloadCompleto();
        QuoteSubmittedPayload b = payloadCompleto();

        assertEquals(a.hashCode(), a.hashCode());
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void toString_ShouldContainFieldValues() {
        String result = payloadCompleto().toString();

        assertNotNull(result);
        assertTrue(result.contains("quote-1"));
        assertTrue(result.contains("PENDING"));
    }

    @Test
    void equals_ShouldHandleNullFields() {
        QuoteSubmittedPayload empty1 = new QuoteSubmittedPayload();
        QuoteSubmittedPayload empty2 = new QuoteSubmittedPayload();
        QuoteSubmittedPayload full = payloadCompleto();

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
        QuoteSubmittedPayload a = payloadCompleto();
        assertTrue(a.equals(a));

        QuoteSubmittedPayload sub = new QuoteSubmittedPayload() {
            @Override
            public boolean canEqual(Object o) {
                return false;
            }
        };
        assertFalse(a.equals(sub));

        QuoteSubmittedPayload partial1 = new QuoteSubmittedPayload();
        partial1.setQuoteId("A");
        QuoteSubmittedPayload partial2 = new QuoteSubmittedPayload();
        partial2.setStatus("B");
        assertNotEquals(partial1, partial2);
        assertNotEquals(partial2, partial1);
    }

    @Test
    void equals_AllSubsets() {
        java.util.List<QuoteSubmittedPayload> objs = new java.util.ArrayList<>();
        for (int mask = 0; mask < 32; mask++) {
            QuoteSubmittedPayload o = new QuoteSubmittedPayload();
            if ((mask & 1) != 0) o.setQuoteId("Q");
            if ((mask & 2) != 0) o.setTotal(java.math.BigDecimal.ONE);
            if ((mask & 4) != 0) o.setCurrency("BRL");
            if ((mask & 8) != 0) o.setStatus("PENDING");
            if ((mask & 16) != 0) o.setValidUntil(java.time.Instant.parse("2020-01-01T00:00:00Z"));
            objs.add(o);
        }
        for (QuoteSubmittedPayload a : objs) {
            for (QuoteSubmittedPayload b : objs) {
                a.equals(b);
            }
            a.hashCode();
            a.toString();
        }
    }
}
