package com.f1rsters.os_service.dto;

import com.f1rsters.os_service.domain.StatusOrdemServico;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrdemServicoDTOTest {

    private OrdemServicoDTO dtoCompleto() {
        OrdemServicoDTO dto = new OrdemServicoDTO();
        dto.id = 1L;
        dto.clienteId = "12345678909";
        dto.veiculoId = "ABC1234";
        dto.servicosIds = List.of("S1", "S2");
        dto.pecasIds = List.of("P1");
        dto.valorTotal = BigDecimal.valueOf(1000.0);
        dto.status = StatusOrdemServico.RECEBIDA;
        dto.criadoEm = LocalDateTime.of(2026, 1, 1, 10, 0);
        dto.atualizadoEm = LocalDateTime.of(2026, 1, 1, 11, 0);
        return dto;
    }

    @Test
    void dto_ShouldAcceptFields() {
        OrdemServicoDTO dto = dtoCompleto();

        assertEquals(1L, dto.getId());
        assertEquals("12345678909", dto.getClienteId());
        assertEquals("ABC1234", dto.getVeiculoId());
        assertEquals(StatusOrdemServico.RECEBIDA, dto.getStatus());
        assertEquals(BigDecimal.valueOf(1000.0), dto.getValorTotal());
        assertEquals(List.of("S1", "S2"), dto.getServicosIds());
        assertEquals(List.of("P1"), dto.getPecasIds());
        assertNotNull(dto.getCriadoEm());
        assertNotNull(dto.getAtualizadoEm());
    }

    @Test
    void equals_ShouldReturnTrueForEqualObjects() {
        OrdemServicoDTO a = dtoCompleto();
        OrdemServicoDTO b = dtoCompleto();

        assertEquals(a, a);
        assertEquals(a, b);
        assertEquals(b, a);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equals_ShouldReturnFalseForDifferentObjects() {
        OrdemServicoDTO base = dtoCompleto();

        assertNotEquals(base, null);
        assertNotEquals(base, "outro objeto");

        OrdemServicoDTO difId = dtoCompleto();
        difId.id = 2L;
        assertNotEquals(base, difId);

        OrdemServicoDTO difCliente = dtoCompleto();
        difCliente.clienteId = "99999999999";
        assertNotEquals(base, difCliente);

        OrdemServicoDTO difVeiculo = dtoCompleto();
        difVeiculo.veiculoId = "XYZ9876";
        assertNotEquals(base, difVeiculo);

        OrdemServicoDTO difServicos = dtoCompleto();
        difServicos.servicosIds = List.of("S9");
        assertNotEquals(base, difServicos);

        OrdemServicoDTO difPecas = dtoCompleto();
        difPecas.pecasIds = List.of("P9");
        assertNotEquals(base, difPecas);

        OrdemServicoDTO difValor = dtoCompleto();
        difValor.valorTotal = BigDecimal.valueOf(2000.0);
        assertNotEquals(base, difValor);

        OrdemServicoDTO difStatus = dtoCompleto();
        difStatus.status = StatusOrdemServico.FINALIZADA;
        assertNotEquals(base, difStatus);

        OrdemServicoDTO difCriadoEm = dtoCompleto();
        difCriadoEm.criadoEm = LocalDateTime.of(2026, 2, 1, 10, 0);
        assertNotEquals(base, difCriadoEm);

        OrdemServicoDTO difAtualizadoEm = dtoCompleto();
        difAtualizadoEm.atualizadoEm = LocalDateTime.of(2026, 2, 1, 11, 0);
        assertNotEquals(base, difAtualizadoEm);

        assertNotEquals(base, new OrdemServicoDTO());
    }

    @Test
    void hashCode_ShouldBeConsistent() {
        OrdemServicoDTO a = dtoCompleto();
        OrdemServicoDTO b = dtoCompleto();
        OrdemServicoDTO vazio = new OrdemServicoDTO();

        assertEquals(a.hashCode(), a.hashCode());
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a.hashCode(), vazio.hashCode());
    }

    @Test
    void canEqual_ShouldBehaveCorrectly() {
        OrdemServicoDTO dto = dtoCompleto();

        assertTrue(dto.canEqual(dto));
        assertTrue(dto.canEqual(dtoCompleto()));
        assertFalse(dto.canEqual("outro objeto"));
        assertFalse(dto.canEqual(null));
    }

    @Test
    void equals_ShouldReturnFalseWhenCanEqualFails() {
        OrdemServicoDTO base = dtoCompleto();
        OrdemServicoDTO sub = new OrdemServicoDTO() {
            @Override
            protected boolean canEqual(Object other) {
                return false;
            }
        };
        sub.id = base.id;
        sub.clienteId = base.clienteId;
        sub.veiculoId = base.veiculoId;
        sub.servicosIds = base.servicosIds;
        sub.pecasIds = base.pecasIds;
        sub.valorTotal = base.valorTotal;
        sub.status = base.status;
        sub.criadoEm = base.criadoEm;
        sub.atualizadoEm = base.atualizadoEm;

        assertNotEquals(base, sub);
    }

    @Test
    void toString_ShouldContainFieldValues() {
        OrdemServicoDTO dto = dtoCompleto();

        String result = dto.toString();

        assertNotNull(result);
        assertTrue(result.contains("12345678909"));
        assertTrue(result.contains("ABC1234"));
        assertTrue(result.contains("RECEBIDA"));
    }
}
