package com.f1rsters.os_service.dto;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CriarOrdemServicoDTOTest {

    private CriarOrdemServicoDTO dtoCompleto() {
        CriarOrdemServicoDTO dto = new CriarOrdemServicoDTO();
        dto.cpfCnpjCliente = "12345678909";
        dto.placaVeiculo = "ABC1234";
        dto.servicosIds = List.of("1", "2");
        dto.pecasIds = List.of("10");
        return dto;
    }

    @Test
    void dto_ShouldAcceptFields() {
        CriarOrdemServicoDTO dto = dtoCompleto();

        assertEquals("12345678909", dto.getCpfCnpjCliente());
        assertEquals("ABC1234", dto.getPlacaVeiculo());
        assertEquals(2, dto.getServicosIds().size());
        assertEquals(1, dto.getPecasIds().size());
    }

    @Test
    void equals_ShouldReturnTrueForEqualObjects() {
        CriarOrdemServicoDTO a = dtoCompleto();
        CriarOrdemServicoDTO b = dtoCompleto();

        assertEquals(a, a);
        assertEquals(a, b);
        assertEquals(b, a);
        assertEquals(a.hashCode(), b.hashCode());
    }

    @Test
    void equals_ShouldReturnFalseForDifferentObjects() {
        CriarOrdemServicoDTO base = dtoCompleto();

        assertNotEquals(base, null);
        assertNotEquals(base, "outro objeto");
        assertNotEquals(base, new Object());

        CriarOrdemServicoDTO difCpf = dtoCompleto();
        difCpf.cpfCnpjCliente = "99999999999";
        assertNotEquals(base, difCpf);

        CriarOrdemServicoDTO difPlaca = dtoCompleto();
        difPlaca.placaVeiculo = "XYZ9876";
        assertNotEquals(base, difPlaca);

        CriarOrdemServicoDTO difServicos = dtoCompleto();
        difServicos.servicosIds = List.of("9");
        assertNotEquals(base, difServicos);

        CriarOrdemServicoDTO difPecas = dtoCompleto();
        difPecas.pecasIds = List.of("99");
        assertNotEquals(base, difPecas);

        CriarOrdemServicoDTO nulo = new CriarOrdemServicoDTO();
        assertNotEquals(base, nulo);
    }

    @Test
    void hashCode_ShouldBeConsistent() {
        CriarOrdemServicoDTO a = dtoCompleto();
        CriarOrdemServicoDTO b = dtoCompleto();
        CriarOrdemServicoDTO vazio = new CriarOrdemServicoDTO();

        assertEquals(a.hashCode(), a.hashCode());
        assertEquals(a.hashCode(), b.hashCode());
        assertNotEquals(a.hashCode(), vazio.hashCode());
    }

    @Test
    void toString_ShouldContainFieldValues() {
        CriarOrdemServicoDTO dto = dtoCompleto();

        String result = dto.toString();

        assertNotNull(result);
        assertTrue(result.contains("12345678909"));
        assertTrue(result.contains("ABC1234"));
    }
}
