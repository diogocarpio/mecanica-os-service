package com.f1rsters.os_service.dto.billing;

import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.*;

class OsCancelledPayloadTest {

    private OsCancelledPayload payloadCompleto() {
        return new OsCancelledPayload(
                "motivo do cancelamento",
                Instant.parse("2026-11-16T20:08:00Z"));
    }

    @Test
    void allArgsConstructor_ShouldPopulateAllFields() {
        OsCancelledPayload payload = payloadCompleto();

        assertEquals("motivo do cancelamento", payload.getReason());
        assertEquals(Instant.parse("2026-11-16T20:08:00Z"), payload.getCancelledAt());
    }

    @Test
    void noArgsConstructor_ShouldAllowSetters() {
        OsCancelledPayload payload = new OsCancelledPayload();
        payload.setReason("outro motivo");
        payload.setCancelledAt(Instant.now());

        assertEquals("outro motivo", payload.getReason());
    }

    @Test
    void equals_ShouldReturnTrueForEqualObjects() {
        OsCancelledPayload a = payloadCompleto();
        OsCancelledPayload b = payloadCompleto();

        assertEquals(a, a);
        assertEquals(a, b);
        assertEquals(b, a);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equals_ShouldReturnFalseForDifferentObjects() {
        OsCancelledPayload base = payloadCompleto();

        assertNotEquals(base, null);
        assertNotEquals(base, "outro objeto");

        OsCancelledPayload difReason = payloadCompleto();
        difReason.setReason("outro");
        assertNotEquals(base, difReason);

        OsCancelledPayload difCancelledAt = payloadCompleto();
        difCancelledAt.setCancelledAt(Instant.parse("2026-11-17T00:00:00Z"));
        assertNotEquals(base, difCancelledAt);

        assertNotEquals(base, new OsCancelledPayload());
    }

    @Test
    void hashCode_ShouldBeConsistent() {
        OsCancelledPayload a = payloadCompleto();
        OsCancelledPayload b = payloadCompleto();

        assertEquals(a.hashCode(), a.hashCode());
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void toString_ShouldContainFieldValues() {
        String result = payloadCompleto().toString();

        assertNotNull(result);
        assertTrue(result.contains("motivo do cancelamento"));
    }

    @Test
    void equals_ShouldHandleNullFields() {
        OsCancelledPayload empty1 = new OsCancelledPayload();
        OsCancelledPayload empty2 = new OsCancelledPayload();
        OsCancelledPayload full = payloadCompleto();

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
        OsCancelledPayload a = payloadCompleto();
        OsCancelledPayload mesmoObjeto = a;
        assertTrue(a.equals(mesmoObjeto));

        OsCancelledPayload sub = new OsCancelledPayload() {
            @Override
            public boolean canEqual(Object o) {
                return false;
            }
        };
        assertFalse(a.equals(sub));

        OsCancelledPayload partial1 = new OsCancelledPayload();
        partial1.setReason("A");
        OsCancelledPayload partial2 = new OsCancelledPayload();
        partial2.setCancelledAt(java.time.Instant.parse("2020-01-01T00:00:00Z"));
        assertNotEquals(partial1, partial2);
        assertNotEquals(partial2, partial1);
    }

    @Test
    void equals_AllSubsets() {
        java.util.List<OsCancelledPayload> objs = new java.util.ArrayList<>();
        for (int mask = 0; mask < 4; mask++) {
            OsCancelledPayload o = new OsCancelledPayload();
            if ((mask & 1) != 0) o.setReason("R");
            if ((mask & 2) != 0) o.setCancelledAt(java.time.Instant.parse("2020-01-01T00:00:00Z"));
            objs.add(o);
        }
        for (OsCancelledPayload a : objs) {
            for (OsCancelledPayload b : objs) {
                a.equals(b);
            }
            a.hashCode();
            a.toString();
        }
    }
}
