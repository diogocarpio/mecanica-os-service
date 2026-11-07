package com.f1rsters.os_service.dto;

import com.f1rsters.os_service.domain.StatusOrdemServico;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OSEventTest {

    @Test
    void constructorAndAccessors_ShouldPopulateFields() {
        LocalDateTime timestamp = LocalDateTime.now();
        OSEvent event = new OSEvent(
                10L,
                "12345678909",
                "ABC1234",
                List.of("S1", "S2"),
                List.of("P1"),
                BigDecimal.valueOf(1000),
                StatusOrdemServico.RECEBIDA,
                "os.created"
        );

        assertEquals(10L, event.getOsId());
        assertEquals("12345678909", event.getClienteId());
        assertEquals("ABC1234", event.getVeiculoId());
        assertEquals(List.of("S1", "S2"), event.getServicosIds());
        assertEquals(List.of("P1"), event.getPecasIds());
        assertEquals(BigDecimal.valueOf(1000), event.getValorTotal());
        assertEquals(StatusOrdemServico.RECEBIDA, event.getStatus());
        assertEquals("os.created", event.getEventType());
        assertNotNull(event.getTimestamp());

        OSEvent secondEvent = new OSEvent(
                11L,
                "11111111111",
                "XYZ4321",
                List.of("S3"),
                List.of("P2"),
                BigDecimal.valueOf(250),
                StatusOrdemServico.DIAGNOSTICO,
                "os.status.updated"
        );
        secondEvent.setTimestamp(timestamp);

        assertEquals(11L, secondEvent.getOsId());
        assertEquals("11111111111", secondEvent.getClienteId());
        assertEquals(StatusOrdemServico.DIAGNOSTICO, secondEvent.getStatus());
        assertEquals("os.status.updated", secondEvent.getEventType());
        assertEquals(timestamp, secondEvent.getTimestamp());
    }

    @Test
    void allArgsConstructor_ShouldPopulateAllFieldsIncludingTimestamp() {
        LocalDateTime timestamp = LocalDateTime.of(2026, 1, 1, 12, 0);

        OSEvent event = new OSEvent(
                20L,
                "22222222222",
                "LMN5678",
                List.of("S9"),
                List.of("P9"),
                BigDecimal.valueOf(500),
                StatusOrdemServico.EM_EXECUCAO,
                "os.approved",
                timestamp
        );

        assertEquals(20L, event.getOsId());
        assertEquals("22222222222", event.getClienteId());
        assertEquals("LMN5678", event.getVeiculoId());
        assertEquals(List.of("S9"), event.getServicosIds());
        assertEquals(List.of("P9"), event.getPecasIds());
        assertEquals(BigDecimal.valueOf(500), event.getValorTotal());
        assertEquals(StatusOrdemServico.EM_EXECUCAO, event.getStatus());
        assertEquals("os.approved", event.getEventType());
        assertEquals(timestamp, event.getTimestamp());
    }

    private OSEvent eventoCompleto() {
        return new OSEvent(
                10L,
                "12345678909",
                "ABC1234",
                List.of("S1"),
                List.of("P1"),
                BigDecimal.valueOf(1000),
                StatusOrdemServico.RECEBIDA,
                "os.created",
                LocalDateTime.of(2026, 1, 1, 10, 0)
        );
    }

    @Test
    void equals_ShouldReturnTrueForEqualObjects() {
        OSEvent a = eventoCompleto();
        OSEvent b = eventoCompleto();

        assertEquals(a, a);
        assertEquals(a, b);
        assertEquals(b, a);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equals_ShouldReturnFalseForDifferentObjects() {
        OSEvent base = eventoCompleto();

        assertNotEquals(base, null);
        assertNotEquals(base, "outro objeto");

        OSEvent difOsId = eventoCompleto();
        difOsId.setOsId(99L);
        assertNotEquals(base, difOsId);

        OSEvent difCliente = eventoCompleto();
        difCliente.setClienteId("99999999999");
        assertNotEquals(base, difCliente);

        OSEvent difVeiculo = eventoCompleto();
        difVeiculo.setVeiculoId("XYZ9876");
        assertNotEquals(base, difVeiculo);

        OSEvent difServicos = eventoCompleto();
        difServicos.setServicosIds(List.of("S9"));
        assertNotEquals(base, difServicos);

        OSEvent difPecas = eventoCompleto();
        difPecas.setPecasIds(List.of("P9"));
        assertNotEquals(base, difPecas);

        OSEvent difValor = eventoCompleto();
        difValor.setValorTotal(BigDecimal.valueOf(2000));
        assertNotEquals(base, difValor);

        OSEvent difStatus = eventoCompleto();
        difStatus.setStatus(StatusOrdemServico.CANCELADA);
        assertNotEquals(base, difStatus);

        OSEvent difEventType = eventoCompleto();
        difEventType.setEventType("os.cancelled");
        assertNotEquals(base, difEventType);

        OSEvent difTimestamp = eventoCompleto();
        difTimestamp.setTimestamp(LocalDateTime.of(2026, 2, 1, 10, 0));
        assertNotEquals(base, difTimestamp);

        OSEvent vazio = new OSEvent();
        assertNotEquals(base, vazio);
    }

    @Test
    void hashCode_ShouldBeConsistent() {
        OSEvent a = eventoCompleto();
        OSEvent b = eventoCompleto();
        OSEvent vazio = new OSEvent();

        assertEquals(a.hashCode(), a.hashCode());
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a.hashCode(), vazio.hashCode());
    }

    @Test
    void toString_ShouldContainFieldValues() {
        OSEvent event = eventoCompleto();

        String result = event.toString();

        assertNotNull(result);
        assertTrue(result.contains("os.created"));
        assertTrue(result.contains("ABC1234"));
    }
}
