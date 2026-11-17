package com.f1rsters.os_service.dto.billing;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class IntegrationEventTest {

    private IntegrationEvent<String> eventoCompleto() {
        return new IntegrationEvent<>(
                UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"),
                "os.quote.requested.v1",
                1,
                Instant.parse("2026-11-16T20:00:00Z"),
                "os-service",
                "42",
                UUID.fromString("6fa85f64-5717-4562-b3fc-2c963f66afa7"),
                "42",
                "payload");
    }

    @Test
    void allArgsConstructor_ShouldPopulateAllFields() {
        IntegrationEvent<String> evento = eventoCompleto();

        assertEquals(UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"), evento.getEventId());
        assertEquals("os.quote.requested.v1", evento.getEventType());
        assertEquals(1, evento.getEventVersion());
        assertEquals(Instant.parse("2026-11-16T20:00:00Z"), evento.getOccurredAt());
        assertEquals("os-service", evento.getSource());
        assertEquals("42", evento.getCorrelationId());
        assertEquals(UUID.fromString("6fa85f64-5717-4562-b3fc-2c963f66afa7"), evento.getCausationId());
        assertEquals("42", evento.getOsId());
        assertEquals("payload", evento.getPayload());
    }

    @Test
    void noArgsConstructor_ShouldAllowSetters() {
        IntegrationEvent<String> evento = new IntegrationEvent<>();
        evento.setEventId(UUID.randomUUID());
        evento.setEventType("os.cancelled.v1");
        evento.setEventVersion(2);
        evento.setOccurredAt(Instant.now());
        evento.setSource("os-service");
        evento.setCorrelationId("7");
        evento.setCausationId(UUID.randomUUID());
        evento.setOsId("7");
        evento.setPayload("p");

        assertEquals("os.cancelled.v1", evento.getEventType());
        assertEquals(2, evento.getEventVersion());
        assertEquals("p", evento.getPayload());
    }

    @Test
    void equals_ShouldReturnTrueForEqualObjects() {
        IntegrationEvent<String> a = eventoCompleto();
        IntegrationEvent<String> b = eventoCompleto();

        assertEquals(a, a);
        assertEquals(a, b);
        assertEquals(b, a);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equals_ShouldReturnFalseForDifferentObjects() {
        IntegrationEvent<String> base = eventoCompleto();

        assertNotEquals(base, null);
        assertNotEquals(base, "outro objeto");

        IntegrationEvent<String> difTipo = eventoCompleto();
        difTipo.setEventType("outro.tipo.v1");
        assertNotEquals(base, difTipo);

        IntegrationEvent<String> difVersao = eventoCompleto();
        difVersao.setEventVersion(9);
        assertNotEquals(base, difVersao);

        IntegrationEvent<String> difPayload = eventoCompleto();
        difPayload.setPayload("outro");
        assertNotEquals(base, difPayload);

        assertNotEquals(base, new IntegrationEvent<>());
    }

    @Test
    void hashCode_ShouldBeConsistent() {
        IntegrationEvent<String> a = eventoCompleto();
        IntegrationEvent<String> b = eventoCompleto();

        assertEquals(a.hashCode(), a.hashCode());
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void toString_ShouldContainFieldValues() {
        String result = eventoCompleto().toString();

        assertNotNull(result);
        assertTrue(result.contains("os.quote.requested.v1"));
        assertTrue(result.contains("os-service"));
    }

    @Test
    void equals_ShouldHandleNullFields() {
        IntegrationEvent empty1 = new IntegrationEvent();
        IntegrationEvent empty2 = new IntegrationEvent();
        IntegrationEvent full = eventoCompleto();

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
        IntegrationEvent<String> a = eventoCompleto();
        assertTrue(a.equals(a));

        IntegrationEvent<String> sub = new IntegrationEvent<String>() {
            @Override
            public boolean canEqual(Object o) {
                return false;
            }
        };
        assertFalse(a.equals(sub));

        IntegrationEvent<String> partial1 = new IntegrationEvent<String>();
        partial1.setEventType("A");
        IntegrationEvent<String> partial2 = new IntegrationEvent<String>();
        partial2.setSource("B");
        assertNotEquals(partial1, partial2);
        assertNotEquals(partial2, partial1);
    }

    @Test
    void equals_AllSubsets() {
        java.util.List<IntegrationEvent<String>> objs = new java.util.ArrayList<>();
        for (int mask = 0; mask < 512; mask++) {
            IntegrationEvent<String> o = new IntegrationEvent<String>();
            if ((mask & 1) != 0) o.setEventId(java.util.UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"));
            if ((mask & 2) != 0) o.setEventType("T");
            if ((mask & 4) != 0) o.setEventVersion(1);
            if ((mask & 8) != 0) o.setOccurredAt(java.time.Instant.parse("2020-01-01T00:00:00Z"));
            if ((mask & 16) != 0) o.setSource("S");
            if ((mask & 32) != 0) o.setCorrelationId("C");
            if ((mask & 64) != 0) o.setCausationId(java.util.UUID.fromString("3fa85f64-5717-4562-b3fc-2c963f66afa6"));
            if ((mask & 128) != 0) o.setOsId("O");
            if ((mask & 256) != 0) o.setPayload("P");
            objs.add(o);
        }
        for (IntegrationEvent<String> a : objs) {
            for (IntegrationEvent<String> b : objs) {
                a.equals(b);
            }
            a.hashCode();
            a.toString();
        }
    }
}
