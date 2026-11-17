package com.f1rsters.os_service.dto.billing;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class QuoteFailedPayloadTest {

    private QuoteFailedPayload payloadCompleto() {
        return new QuoteFailedPayload(
                "INVALID_QUOTE_DATA",
                "detalhe do erro",
                Instant.parse("2026-11-16T20:07:00Z"));
    }

    @Test
    void allArgsConstructor_ShouldPopulateAllFields() {
        QuoteFailedPayload payload = payloadCompleto();

        assertEquals("INVALID_QUOTE_DATA", payload.getCode());
        assertEquals("detalhe do erro", payload.getMessage());
        assertEquals(Instant.parse("2026-11-16T20:07:00Z"), payload.getFailedAt());
    }

    @Test
    void noArgsConstructor_ShouldAllowSetters() {
        QuoteFailedPayload payload = new QuoteFailedPayload();
        payload.setCode("OTHER");
        payload.setMessage("outra mensagem");
        payload.setFailedAt(Instant.now());

        assertEquals("OTHER", payload.getCode());
        assertEquals("outra mensagem", payload.getMessage());
    }

    @Test
    void equals_ShouldReturnTrueForEqualObjects() {
        QuoteFailedPayload a = payloadCompleto();
        QuoteFailedPayload b = payloadCompleto();

        assertEquals(a, a);
        assertEquals(a, b);
        assertEquals(b, a);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equals_ShouldReturnFalseForDifferentObjects() {
        QuoteFailedPayload base = payloadCompleto();

        assertNotEquals(base, null);
        assertNotEquals(base, "outro objeto");

        QuoteFailedPayload difCode = payloadCompleto();
        difCode.setCode("OTHER");
        assertNotEquals(base, difCode);

        QuoteFailedPayload difMessage = payloadCompleto();
        difMessage.setMessage("outra");
        assertNotEquals(base, difMessage);

        assertNotEquals(base, new QuoteFailedPayload());
    }

    @Test
    void hashCode_ShouldBeConsistent() {
        QuoteFailedPayload a = payloadCompleto();
        QuoteFailedPayload b = payloadCompleto();

        assertEquals(a.hashCode(), a.hashCode());
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void toString_ShouldContainFieldValues() {
        String result = payloadCompleto().toString();

        assertNotNull(result);
        assertTrue(result.contains("INVALID_QUOTE_DATA"));
        assertTrue(result.contains("detalhe do erro"));
    }

    @Test
    void equals_ShouldHandleNullFields() {
        QuoteFailedPayload empty1 = new QuoteFailedPayload();
        QuoteFailedPayload empty2 = new QuoteFailedPayload();
        QuoteFailedPayload full = payloadCompleto();

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
        QuoteFailedPayload a = payloadCompleto();
        QuoteFailedPayload mesmoObjeto = a;
        assertTrue(a.equals(mesmoObjeto));

        QuoteFailedPayload sub = new QuoteFailedPayload() {
            @Override
            public boolean canEqual(Object o) {
                return false;
            }
        };
        assertFalse(a.equals(sub));

        QuoteFailedPayload partial1 = new QuoteFailedPayload();
        partial1.setCode("A");
        QuoteFailedPayload partial2 = new QuoteFailedPayload();
        partial2.setMessage("B");
        assertNotEquals(partial1, partial2);
        assertNotEquals(partial2, partial1);
    }
    @Test
    void equals_Matrix() {
        QuoteFailedPayload full = payloadCompleto();
        QuoteFailedPayload empty = new QuoteFailedPayload();
        QuoteFailedPayload onlyCode = new QuoteFailedPayload();
        onlyCode.setCode("X");
        QuoteFailedPayload onlyMessage = new QuoteFailedPayload();
        onlyMessage.setMessage("Y");
        QuoteFailedPayload onlyFailedAt = new QuoteFailedPayload();
        onlyFailedAt.setFailedAt(java.time.Instant.parse("2020-01-01T00:00:00Z"));
        QuoteFailedPayload diffCode = payloadCompleto();
        diffCode.setCode("Z");
        QuoteFailedPayload diffMessage = payloadCompleto();
        diffMessage.setMessage("W");
        QuoteFailedPayload diffFailedAt = payloadCompleto();
        diffFailedAt.setFailedAt(java.time.Instant.parse("2021-01-01T00:00:00Z"));

        QuoteFailedPayload fullRef = full;
        QuoteFailedPayload emptyRef = empty;
        QuoteFailedPayload onlyCodeRef = onlyCode;

        assertTrue(full.equals(fullRef));
        assertTrue(empty.equals(emptyRef));
        assertFalse(full.equals(empty));
        assertFalse(empty.equals(full));
        assertFalse(onlyCode.equals(onlyMessage));
        assertFalse(onlyCode.equals(onlyFailedAt));
        assertFalse(onlyMessage.equals(onlyFailedAt));
        assertFalse(full.equals(diffCode));
        assertFalse(full.equals(diffMessage));
        assertFalse(full.equals(diffFailedAt));
        assertTrue(onlyCode.equals(onlyCodeRef));
    }

    @Test
    void equals_AllSubsets() {
        java.util.List<QuoteFailedPayload> objs = new java.util.ArrayList<>();
        for (int mask = 0; mask < 8; mask++) {
            QuoteFailedPayload o = new QuoteFailedPayload();
            if ((mask & 1) != 0) o.setCode("C" + mask);
            if ((mask & 2) != 0) o.setMessage("M" + mask);
            if ((mask & 4) != 0) o.setFailedAt(java.time.Instant.parse("2020-01-01T00:00:00Z"));
            objs.add(o);
        }
        for (QuoteFailedPayload a : objs) {
            for (QuoteFailedPayload b : objs) {
                a.equals(b);
                a.hashCode();
                a.toString();
            }
        }
    }
}
