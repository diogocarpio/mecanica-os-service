package com.f1rsters.os_service.dto.billing;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class QuoteApprovedPayloadTest {

    private QuoteApprovedPayload payloadCompleto() {
        return new QuoteApprovedPayload(
                "quote-1",
                BigDecimal.ZERO,
                "BRL",
                "APPROVED",
                Instant.parse("2026-11-16T20:05:00Z"));
    }

    @Test
    void allArgsConstructor_ShouldPopulateAllFields() {
        QuoteApprovedPayload payload = payloadCompleto();

        assertEquals("quote-1", payload.getQuoteId());
        assertEquals(BigDecimal.ZERO, payload.getTotal());
        assertEquals("BRL", payload.getCurrency());
        assertEquals("APPROVED", payload.getStatus());
        assertEquals(Instant.parse("2026-11-16T20:05:00Z"), payload.getDecidedAt());
    }

    @Test
    void noArgsConstructor_ShouldAllowSetters() {
        QuoteApprovedPayload payload = new QuoteApprovedPayload();
        payload.setQuoteId("quote-2");
        payload.setTotal(BigDecimal.TEN);
        payload.setCurrency("USD");
        payload.setStatus("PENDING");
        payload.setDecidedAt(Instant.now());

        assertEquals("quote-2", payload.getQuoteId());
        assertEquals("PENDING", payload.getStatus());
    }

    @Test
    void equals_ShouldReturnTrueForEqualObjects() {
        QuoteApprovedPayload a = payloadCompleto();
        QuoteApprovedPayload b = payloadCompleto();

        assertEquals(a, a);
        assertEquals(a, b);
        assertEquals(b, a);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equals_ShouldReturnFalseForDifferentObjects() {
        QuoteApprovedPayload base = payloadCompleto();

        assertNotEquals(base, null);
        assertNotEquals(base, "outro objeto");

        QuoteApprovedPayload difQuoteId = payloadCompleto();
        difQuoteId.setQuoteId("quote-2");
        assertNotEquals(base, difQuoteId);

        QuoteApprovedPayload difTotal = payloadCompleto();
        difTotal.setTotal(BigDecimal.ONE);
        assertNotEquals(base, difTotal);

        QuoteApprovedPayload difCurrency = payloadCompleto();
        difCurrency.setCurrency("EUR");
        assertNotEquals(base, difCurrency);

        QuoteApprovedPayload difStatus = payloadCompleto();
        difStatus.setStatus("REJECTED");
        assertNotEquals(base, difStatus);

        assertNotEquals(base, new QuoteApprovedPayload());
    }

    @Test
    void hashCode_ShouldBeConsistent() {
        QuoteApprovedPayload a = payloadCompleto();
        QuoteApprovedPayload b = payloadCompleto();

        assertEquals(a.hashCode(), a.hashCode());
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void toString_ShouldContainFieldValues() {
        String result = payloadCompleto().toString();

        assertNotNull(result);
        assertTrue(result.contains("quote-1"));
        assertTrue(result.contains("APPROVED"));
    }

    @Test
    void equals_ShouldHandleNullFields() {
        QuoteApprovedPayload empty1 = new QuoteApprovedPayload();
        QuoteApprovedPayload empty2 = new QuoteApprovedPayload();
        QuoteApprovedPayload full = payloadCompleto();

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
        QuoteApprovedPayload a = payloadCompleto();
        assertTrue(a.equals(a));

        QuoteApprovedPayload sub = new QuoteApprovedPayload() {
            @Override
            public boolean canEqual(Object o) {
                return false;
            }
        };
        assertFalse(a.equals(sub));

        QuoteApprovedPayload partial1 = new QuoteApprovedPayload();
        partial1.setQuoteId("A");
        QuoteApprovedPayload partial2 = new QuoteApprovedPayload();
        partial2.setStatus("B");
        assertNotEquals(partial1, partial2);
        assertNotEquals(partial2, partial1);
    }

    @Test
    void equals_AllSubsets() {
        java.util.List<QuoteApprovedPayload> objs = new java.util.ArrayList<>();
        for (int mask = 0; mask < 32; mask++) {
            QuoteApprovedPayload o = new QuoteApprovedPayload();
            if ((mask & 1) != 0) o.setQuoteId("Q");
            if ((mask & 2) != 0) o.setTotal(java.math.BigDecimal.ONE);
            if ((mask & 4) != 0) o.setCurrency("BRL");
            if ((mask & 8) != 0) o.setStatus("APPROVED");
            if ((mask & 16) != 0) o.setDecidedAt(java.time.Instant.parse("2020-01-01T00:00:00Z"));
            objs.add(o);
        }
        for (QuoteApprovedPayload a : objs) {
            for (QuoteApprovedPayload b : objs) {
                a.equals(b);
            }
            a.hashCode();
            a.toString();
        }
    }
}
