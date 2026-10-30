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
}
