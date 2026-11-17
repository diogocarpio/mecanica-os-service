package com.f1rsters.os_service.dto.billing;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class QuoteRequestedPayloadTest {

    private QuoteRequestedPayload payloadCompleto() {
        return new QuoteRequestedPayload(
                "10000000000",
                List.of(new QuoteItemPayload("Servico", "LABOR", BigDecimal.ONE, BigDecimal.ZERO)),
                "BRL",
                Instant.parse("2026-11-23T20:00:00Z"));
    }

    @Test
    void allArgsConstructor_ShouldPopulateAllFields() {
        QuoteRequestedPayload payload = payloadCompleto();

        assertEquals("10000000000", payload.getCustomerId());
        assertEquals(1, payload.getItems().size());
        assertEquals("BRL", payload.getCurrency());
        assertEquals(Instant.parse("2026-11-23T20:00:00Z"), payload.getValidUntil());
    }

    @Test
    void noArgsConstructor_ShouldAllowSetters() {
        QuoteRequestedPayload payload = new QuoteRequestedPayload();
        payload.setCustomerId("20000000000");
        payload.setItems(List.of());
        payload.setCurrency("USD");
        payload.setValidUntil(Instant.now());

        assertEquals("20000000000", payload.getCustomerId());
        assertEquals("USD", payload.getCurrency());
    }

    @Test
    void equals_ShouldReturnTrueForEqualObjects() {
        QuoteRequestedPayload a = payloadCompleto();
        QuoteRequestedPayload b = payloadCompleto();

        assertEquals(a, a);
        assertEquals(a, b);
        assertEquals(b, a);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equals_ShouldReturnFalseForDifferentObjects() {
        QuoteRequestedPayload base = payloadCompleto();

        assertNotEquals(base, null);
        assertNotEquals(base, "outro objeto");

        QuoteRequestedPayload difCustomer = payloadCompleto();
        difCustomer.setCustomerId("30000000000");
        assertNotEquals(base, difCustomer);

        QuoteRequestedPayload difItems = payloadCompleto();
        difItems.setItems(List.of());
        assertNotEquals(base, difItems);

        QuoteRequestedPayload difCurrency = payloadCompleto();
        difCurrency.setCurrency("EUR");
        assertNotEquals(base, difCurrency);

        assertNotEquals(base, new QuoteRequestedPayload());
    }

    @Test
    void hashCode_ShouldBeConsistent() {
        QuoteRequestedPayload a = payloadCompleto();
        QuoteRequestedPayload b = payloadCompleto();

        assertEquals(a.hashCode(), a.hashCode());
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void toString_ShouldContainFieldValues() {
        String result = payloadCompleto().toString();

        assertNotNull(result);
        assertTrue(result.contains("10000000000"));
        assertTrue(result.contains("BRL"));
    }

    @Test
    void equals_ShouldHandleNullFields() {
        QuoteRequestedPayload empty1 = new QuoteRequestedPayload();
        QuoteRequestedPayload empty2 = new QuoteRequestedPayload();
        QuoteRequestedPayload full = payloadCompleto();

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
        QuoteRequestedPayload a = payloadCompleto();
        QuoteRequestedPayload mesmoObjeto = a;
        assertTrue(a.equals(mesmoObjeto));

        QuoteRequestedPayload sub = new QuoteRequestedPayload() {
            @Override
            public boolean canEqual(Object o) {
                return false;
            }
        };
        assertFalse(a.equals(sub));

        QuoteRequestedPayload partial1 = new QuoteRequestedPayload();
        partial1.setCustomerId("A");
        QuoteRequestedPayload partial2 = new QuoteRequestedPayload();
        partial2.setCurrency("B");
        assertNotEquals(partial1, partial2);
        assertNotEquals(partial2, partial1);
    }

    @Test
    void equals_AllSubsets() {
        java.util.List<QuoteRequestedPayload> objs = new java.util.ArrayList<>();
        for (int mask = 0; mask < 16; mask++) {
            QuoteRequestedPayload o = new QuoteRequestedPayload();
            if ((mask & 1) != 0) o.setCustomerId("C");
            if ((mask & 2) != 0) o.setItems(java.util.List.of());
            if ((mask & 4) != 0) o.setCurrency("BRL");
            if ((mask & 8) != 0) o.setValidUntil(java.time.Instant.parse("2020-01-01T00:00:00Z"));
            objs.add(o);
        }
        for (QuoteRequestedPayload a : objs) {
            for (QuoteRequestedPayload b : objs) {
                a.equals(b);
            }
            a.hashCode();
            a.toString();
        }
    }
}
