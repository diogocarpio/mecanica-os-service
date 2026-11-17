package com.f1rsters.os_service.dto.billing;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class QuoteRejectedPayloadTest {

    private QuoteRejectedPayload payloadCompleto() {
        return new QuoteRejectedPayload(
                "quote-1",
                "REJECTED",
                "motivo",
                Instant.parse("2026-11-16T20:06:00Z"));
    }

    @Test
    void allArgsConstructor_ShouldPopulateAllFields() {
        QuoteRejectedPayload payload = payloadCompleto();

        assertEquals("quote-1", payload.getQuoteId());
        assertEquals("REJECTED", payload.getStatus());
        assertEquals("motivo", payload.getReason());
        assertEquals(Instant.parse("2026-11-16T20:06:00Z"), payload.getDecidedAt());
    }

    @Test
    void noArgsConstructor_ShouldAllowSetters() {
        QuoteRejectedPayload payload = new QuoteRejectedPayload();
        payload.setQuoteId("quote-2");
        payload.setStatus("APPROVED");
        payload.setReason("outro motivo");
        payload.setDecidedAt(Instant.now());

        assertEquals("quote-2", payload.getQuoteId());
        assertEquals("outro motivo", payload.getReason());
    }

    @Test
    void equals_ShouldReturnTrueForEqualObjects() {
        QuoteRejectedPayload a = payloadCompleto();
        QuoteRejectedPayload b = payloadCompleto();

        assertEquals(a, a);
        assertEquals(a, b);
        assertEquals(b, a);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equals_ShouldReturnFalseForDifferentObjects() {
        QuoteRejectedPayload base = payloadCompleto();

        assertNotEquals(base, null);
        assertNotEquals(base, "outro objeto");

        QuoteRejectedPayload difQuoteId = payloadCompleto();
        difQuoteId.setQuoteId("quote-2");
        assertNotEquals(base, difQuoteId);

        QuoteRejectedPayload difStatus = payloadCompleto();
        difStatus.setStatus("APPROVED");
        assertNotEquals(base, difStatus);

        QuoteRejectedPayload difReason = payloadCompleto();
        difReason.setReason("outro");
        assertNotEquals(base, difReason);

        assertNotEquals(base, new QuoteRejectedPayload());
    }

    @Test
    void hashCode_ShouldBeConsistent() {
        QuoteRejectedPayload a = payloadCompleto();
        QuoteRejectedPayload b = payloadCompleto();

        assertEquals(a.hashCode(), a.hashCode());
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void toString_ShouldContainFieldValues() {
        String result = payloadCompleto().toString();

        assertNotNull(result);
        assertTrue(result.contains("quote-1"));
        assertTrue(result.contains("motivo"));
    }

    @Test
    void equals_ShouldHandleNullFields() {
        QuoteRejectedPayload empty1 = new QuoteRejectedPayload();
        QuoteRejectedPayload empty2 = new QuoteRejectedPayload();
        QuoteRejectedPayload full = payloadCompleto();

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
        QuoteRejectedPayload a = payloadCompleto();
        assertTrue(a.equals(a));

        QuoteRejectedPayload sub = new QuoteRejectedPayload() {
            @Override
            public boolean canEqual(Object o) {
                return false;
            }
        };
        assertFalse(a.equals(sub));

        QuoteRejectedPayload partial1 = new QuoteRejectedPayload();
        partial1.setQuoteId("A");
        QuoteRejectedPayload partial2 = new QuoteRejectedPayload();
        partial2.setReason("B");
        assertNotEquals(partial1, partial2);
        assertNotEquals(partial2, partial1);
    }

    @Test
    void equals_AllSubsets() {
        java.util.List<QuoteRejectedPayload> objs = new java.util.ArrayList<>();
        for (int mask = 0; mask < 16; mask++) {
            QuoteRejectedPayload o = new QuoteRejectedPayload();
            if ((mask & 1) != 0) o.setQuoteId("Q");
            if ((mask & 2) != 0) o.setStatus("REJECTED");
            if ((mask & 4) != 0) o.setReason("R");
            if ((mask & 8) != 0) o.setDecidedAt(java.time.Instant.parse("2020-01-01T00:00:00Z"));
            objs.add(o);
        }
        for (QuoteRejectedPayload a : objs) {
            for (QuoteRejectedPayload b : objs) {
                a.equals(b);
            }
            a.hashCode();
            a.toString();
        }
    }
}
