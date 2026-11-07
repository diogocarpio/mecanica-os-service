package com.f1rsters.os_service.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrdemServicoTest {

    @Test
    void settersAndGetters_ShouldWork() {
        OrdemServico os = new OrdemServico();
        os.setId(1L);
        os.setClienteId("12345678909");
        os.setVeiculoId("ABC1234");
        os.setStatus(StatusOrdemServico.RECEBIDA);
        os.setValorTotal(BigDecimal.valueOf(1000.0));
        os.setServicosIds(List.of("S1", "S2"));
        os.setPecasIds(List.of("P1"));
        os.setCriadoEm(LocalDateTime.now());
        os.setAtualizadoEm(LocalDateTime.now());

        assertEquals(1L, os.getId());
        assertEquals("12345678909", os.getClienteId());
        assertEquals("ABC1234", os.getVeiculoId());
        assertEquals(StatusOrdemServico.RECEBIDA, os.getStatus());
        assertEquals(BigDecimal.valueOf(1000.0), os.getValorTotal());
        assertEquals(List.of("S1", "S2"), os.getServicosIds());
        assertEquals(List.of("P1"), os.getPecasIds());
        assertNotNull(os.getCriadoEm());
        assertNotNull(os.getAtualizadoEm());
    }

    @Test
    void deletedAt_ShouldAcceptValues() {
        OrdemServico os = new OrdemServico();
        LocalDateTime deletedAt = LocalDateTime.now();

        os.setDeletedAt(deletedAt);

        assertEquals(deletedAt, os.getDeletedAt());
        assertNull(new OrdemServico().getDeletedAt());
    }

    private OrdemServico osCompleto() {
        OrdemServico os = new OrdemServico();
        os.setId(1L);
        os.setClienteId("12345678909");
        os.setVeiculoId("ABC1234");
        os.setServicosIds(List.of("S1"));
        os.setPecasIds(List.of("P1"));
        os.setValorTotal(BigDecimal.valueOf(1000.0));
        os.setStatus(StatusOrdemServico.RECEBIDA);
        os.setCriadoEm(LocalDateTime.of(2026, 1, 1, 10, 0));
        os.setAtualizadoEm(LocalDateTime.of(2026, 1, 1, 11, 0));
        os.setDeletedAt(LocalDateTime.of(2026, 1, 2, 10, 0));
        return os;
    }

    @Test
    void equals_ShouldReturnTrueForEqualObjects() {
        OrdemServico a = osCompleto();
        OrdemServico b = osCompleto();

        assertEquals(a, a);
        assertEquals(a, b);
        assertEquals(b, a);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equals_ShouldReturnFalseForDifferentObjects() {
        OrdemServico base = osCompleto();

        assertNotEquals(base, null);
        assertNotEquals(base, "outro objeto");

        OrdemServico difId = osCompleto();
        difId.setId(2L);
        assertNotEquals(base, difId);

        OrdemServico difCliente = osCompleto();
        difCliente.setClienteId("99999999999");
        assertNotEquals(base, difCliente);

        OrdemServico difVeiculo = osCompleto();
        difVeiculo.setVeiculoId("XYZ9876");
        assertNotEquals(base, difVeiculo);

        OrdemServico difServicos = osCompleto();
        difServicos.setServicosIds(List.of("S9"));
        assertNotEquals(base, difServicos);

        OrdemServico difPecas = osCompleto();
        difPecas.setPecasIds(List.of("P9"));
        assertNotEquals(base, difPecas);

        OrdemServico difValor = osCompleto();
        difValor.setValorTotal(BigDecimal.valueOf(2000.0));
        assertNotEquals(base, difValor);

        OrdemServico difStatus = osCompleto();
        difStatus.setStatus(StatusOrdemServico.FINALIZADA);
        assertNotEquals(base, difStatus);

        OrdemServico difCriadoEm = osCompleto();
        difCriadoEm.setCriadoEm(LocalDateTime.of(2026, 2, 1, 10, 0));
        assertNotEquals(base, difCriadoEm);

        OrdemServico difAtualizadoEm = osCompleto();
        difAtualizadoEm.setAtualizadoEm(LocalDateTime.of(2026, 2, 1, 11, 0));
        assertNotEquals(base, difAtualizadoEm);

        OrdemServico difDeletedAt = osCompleto();
        difDeletedAt.setDeletedAt(null);
        assertNotEquals(base, difDeletedAt);

        assertNotEquals(base, new OrdemServico());
    }

    @Test
    void hashCode_ShouldBeConsistent() {
        OrdemServico a = osCompleto();
        OrdemServico b = osCompleto();
        OrdemServico vazio = new OrdemServico();

        assertEquals(a.hashCode(), a.hashCode());
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a.hashCode(), vazio.hashCode());
    }

    @Test
    void toString_ShouldContainFieldValues() {
        OrdemServico os = osCompleto();

        String result = os.toString();

        assertNotNull(result);
        assertTrue(result.contains("12345678909"));
        assertTrue(result.contains("ABC1234"));
        assertTrue(result.contains("RECEBIDA"));
    }

    @Test
    void lifecycleMethods_ShouldUpdateAuditFields() {
        OrdemServico os = new OrdemServico();

        os.onCreate();
        LocalDateTime beforeUpdate = LocalDateTime.now();
        os.onUpdate();

        assertNotNull(os.getCriadoEm());
        assertNotNull(os.getAtualizadoEm());
        assertTrue(os.getAtualizadoEm().isAfter(os.getCriadoEm()) || os.getAtualizadoEm().isEqual(os.getCriadoEm()));
        assertTrue(os.getCriadoEm().isBefore(beforeUpdate) || os.getCriadoEm().isEqual(beforeUpdate) || os.getCriadoEm().isAfter(beforeUpdate));
    }
}
