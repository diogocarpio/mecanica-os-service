package com.f1rsters.os_service.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class OrdemServicoHistoricoTest {

    private OrdemServicoHistorico historicoCompleto() {
        OrdemServicoHistorico h = new OrdemServicoHistorico();
        h.setId(1L);
        h.setOsId(10L);
        h.setStatusAnterior(StatusOrdemServico.RECEBIDA);
        h.setStatusNovo(StatusOrdemServico.DIAGNOSTICO);
        h.setOrigem("ATUALIZACAO_MANUAL");
        h.setMotivo("diagnóstico iniciado");
        h.setCriadoEm(LocalDateTime.of(2026, 1, 1, 10, 0));
        return h;
    }

    @Test
    void settersAndGetters_ShouldWork() {
        OrdemServicoHistorico h = historicoCompleto();

        assertEquals(1L, h.getId());
        assertEquals(10L, h.getOsId());
        assertEquals(StatusOrdemServico.RECEBIDA, h.getStatusAnterior());
        assertEquals(StatusOrdemServico.DIAGNOSTICO, h.getStatusNovo());
        assertEquals("ATUALIZACAO_MANUAL", h.getOrigem());
        assertEquals("diagnóstico iniciado", h.getMotivo());
        assertNotNull(h.getCriadoEm());
    }

    @Test
    void onCreate_ShouldSetCriadoEm() {
        OrdemServicoHistorico h = new OrdemServicoHistorico();

        h.onCreate();

        assertNotNull(h.getCriadoEm());
    }

    @Test
    void equals_ShouldReturnTrueForEqualObjects() {
        OrdemServicoHistorico a = historicoCompleto();
        OrdemServicoHistorico b = historicoCompleto();

        assertEquals(a, a);
        assertEquals(a, b);
        assertEquals(b, a);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equals_ShouldReturnFalseForDifferentObjects() {
        OrdemServicoHistorico base = historicoCompleto();

        assertNotEquals(base, null);
        assertNotEquals(base, "outro objeto");

        OrdemServicoHistorico difId = historicoCompleto();
        difId.setId(2L);
        assertNotEquals(base, difId);

        OrdemServicoHistorico difOsId = historicoCompleto();
        difOsId.setOsId(99L);
        assertNotEquals(base, difOsId);

        OrdemServicoHistorico difAnterior = historicoCompleto();
        difAnterior.setStatusAnterior(null);
        assertNotEquals(base, difAnterior);

        OrdemServicoHistorico difNovo = historicoCompleto();
        difNovo.setStatusNovo(StatusOrdemServico.FINALIZADA);
        assertNotEquals(base, difNovo);

        OrdemServicoHistorico difOrigem = historicoCompleto();
        difOrigem.setOrigem("SAGA:os.cancelled");
        assertNotEquals(base, difOrigem);

        OrdemServicoHistorico difMotivo = historicoCompleto();
        difMotivo.setMotivo(null);
        assertNotEquals(base, difMotivo);

        OrdemServicoHistorico difData = historicoCompleto();
        difData.setCriadoEm(LocalDateTime.of(2026, 2, 1, 10, 0));
        assertNotEquals(base, difData);

        assertNotEquals(base, new OrdemServicoHistorico());
    }

    @Test
    void hashCode_ShouldBeConsistent() {
        OrdemServicoHistorico a = historicoCompleto();
        OrdemServicoHistorico b = historicoCompleto();
        OrdemServicoHistorico vazio = new OrdemServicoHistorico();

        assertEquals(a.hashCode(), a.hashCode());
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a.hashCode(), vazio.hashCode());
    }

    @Test
    void toString_ShouldContainFieldValues() {
        OrdemServicoHistorico h = historicoCompleto();

        String result = h.toString();

        assertNotNull(result);
        assertTrue(result.contains("ATUALIZACAO_MANUAL"));
        assertTrue(result.contains("DIAGNOSTICO"));
    }
}
