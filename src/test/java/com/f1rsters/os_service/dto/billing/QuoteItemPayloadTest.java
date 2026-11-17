package com.f1rsters.os_service.dto.billing;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class QuoteItemPayloadTest {

    private QuoteItemPayload itemCompleto() {
        return new QuoteItemPayload("Troca de oleo", "LABOR", BigDecimal.ONE, BigDecimal.ZERO);
    }

    @Test
    void allArgsConstructor_ShouldPopulateAllFields() {
        QuoteItemPayload item = itemCompleto();

        assertEquals("Troca de oleo", item.getDescription());
        assertEquals("LABOR", item.getType());
        assertEquals(BigDecimal.ONE, item.getQuantity());
        assertEquals(BigDecimal.ZERO, item.getUnitPrice());
    }

    @Test
    void noArgsConstructor_ShouldAllowSetters() {
        QuoteItemPayload item = new QuoteItemPayload();
        item.setDescription("Peca");
        item.setType("PART");
        item.setQuantity(BigDecimal.TEN);
        item.setUnitPrice(BigDecimal.ONE);

        assertEquals("Peca", item.getDescription());
        assertEquals("PART", item.getType());
        assertEquals(BigDecimal.TEN, item.getQuantity());
        assertEquals(BigDecimal.ONE, item.getUnitPrice());
    }

    @Test
    void equals_ShouldReturnTrueForEqualObjects() {
        QuoteItemPayload a = itemCompleto();
        QuoteItemPayload b = itemCompleto();

        assertEquals(a, a);
        assertEquals(a, b);
        assertEquals(b, a);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equals_ShouldReturnFalseForDifferentObjects() {
        QuoteItemPayload base = itemCompleto();

        assertNotEquals(base, null);
        assertNotEquals(base, "outro objeto");

        QuoteItemPayload difDescription = itemCompleto();
        difDescription.setDescription("Outra");
        assertNotEquals(base, difDescription);

        QuoteItemPayload difType = itemCompleto();
        difType.setType("OTHER");
        assertNotEquals(base, difType);

        QuoteItemPayload difQuantity = itemCompleto();
        difQuantity.setQuantity(BigDecimal.TEN);
        assertNotEquals(base, difQuantity);

        QuoteItemPayload difPrice = itemCompleto();
        difPrice.setUnitPrice(BigDecimal.ONE);
        assertNotEquals(base, difPrice);

        assertNotEquals(base, new QuoteItemPayload());
    }

    @Test
    void hashCode_ShouldBeConsistent() {
        QuoteItemPayload a = itemCompleto();
        QuoteItemPayload b = itemCompleto();

        assertEquals(a.hashCode(), a.hashCode());
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void toString_ShouldContainFieldValues() {
        String result = itemCompleto().toString();

        assertNotNull(result);
        assertTrue(result.contains("Troca de oleo"));
        assertTrue(result.contains("LABOR"));
    }

    @Test
    void equals_ShouldHandleNullFields() {
        QuoteItemPayload empty1 = new QuoteItemPayload();
        QuoteItemPayload empty2 = new QuoteItemPayload();
        QuoteItemPayload full = itemCompleto();

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
        QuoteItemPayload a = itemCompleto();
        assertTrue(a.equals(a));

        QuoteItemPayload sub = new QuoteItemPayload() {
            @Override
            public boolean canEqual(Object o) {
                return false;
            }
        };
        assertFalse(a.equals(sub));

        QuoteItemPayload partial1 = new QuoteItemPayload();
        partial1.setDescription("A");
        QuoteItemPayload partial2 = new QuoteItemPayload();
        partial2.setType("B");
        assertNotEquals(partial1, partial2);
        assertNotEquals(partial2, partial1);
    }

    @Test
    void equals_AllSubsets() {
        java.util.List<QuoteItemPayload> objs = new java.util.ArrayList<>();
        for (int mask = 0; mask < 16; mask++) {
            QuoteItemPayload o = new QuoteItemPayload();
            if ((mask & 1) != 0) o.setDescription("D");
            if ((mask & 2) != 0) o.setType("LABOR");
            if ((mask & 4) != 0) o.setQuantity(java.math.BigDecimal.ONE);
            if ((mask & 8) != 0) o.setUnitPrice(java.math.BigDecimal.ONE);
            objs.add(o);
        }
        for (QuoteItemPayload a : objs) {
            for (QuoteItemPayload b : objs) {
                a.equals(b);
            }
            a.hashCode();
            a.toString();
        }
    }
}
