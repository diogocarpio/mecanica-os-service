package com.f1rsters.os_service.mapper;

import com.f1rsters.os_service.domain.OrdemServico;
import com.f1rsters.os_service.domain.StatusOrdemServico;
import com.f1rsters.os_service.dto.OrdemServicoDTO;
import com.f1rsters.os_service.dto.StatusOrdemServicoDTO;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.lang.reflect.Constructor;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class OrdemServicoMapperTest {

    @Test
    void toDTO_ShouldMapEntityFields() {
        OrdemServico ordemServico = new OrdemServico();
        ordemServico.setId(10L);
        ordemServico.setClienteId("12345678909");
        ordemServico.setVeiculoId("ABC1234");
        ordemServico.setServicosIds(List.of("servico-1", "servico-2"));
        ordemServico.setPecasIds(List.of("peca-1"));
        ordemServico.setValorTotal(new BigDecimal("250.50"));
        ordemServico.setStatus(StatusOrdemServico.RECEBIDA);
        LocalDateTime createdAt = LocalDateTime.of(2024, 1, 10, 8, 30);
        ordemServico.setCriadoEm(createdAt);
        ordemServico.setAtualizadoEm(createdAt.plusMinutes(5));

        OrdemServicoDTO dto = OrdemServicoMapper.toDTO(ordemServico);

        assertNotNull(dto);
        assertEquals(ordemServico.getId(), dto.id);
        assertEquals(ordemServico.getClienteId(), dto.clienteId);
        assertEquals(ordemServico.getVeiculoId(), dto.veiculoId);
        assertEquals(ordemServico.getServicosIds(), dto.servicosIds);
        assertEquals(ordemServico.getPecasIds(), dto.pecasIds);
        assertEquals(ordemServico.getValorTotal(), dto.valorTotal);
        assertEquals(ordemServico.getStatus(), dto.status);
        assertEquals(ordemServico.getCriadoEm(), dto.criadoEm);
        assertEquals(ordemServico.getAtualizadoEm(), dto.atualizadoEm);
    }

    @Test
    void toStatusDTO_ShouldMapStatusAndCreatedDate() {
        OrdemServico ordemServico = new OrdemServico();
        ordemServico.setId(99L);
        ordemServico.setStatus(StatusOrdemServico.EM_EXECUCAO);
        LocalDateTime createdAt = LocalDateTime.of(2024, 5, 2, 12, 0);
        ordemServico.setCriadoEm(createdAt);

        StatusOrdemServicoDTO dto = OrdemServicoMapper.toStatusDTO(ordemServico);

        assertNotNull(dto);
        assertEquals(ordemServico.getId(), dto.osId);
        assertEquals(ordemServico.getStatus(), dto.status);
        assertEquals(ordemServico.getCriadoEm(), dto.criadoEm);
    }

    @Test
    void nullValues_ShouldReturnNull() {
        assertNull(OrdemServicoMapper.toDTO(null));
        assertNull(OrdemServicoMapper.toStatusDTO(null));
    }

    @Test
    void constructor_ShouldBeCallable() throws Exception {
        Constructor<OrdemServicoMapper> constructor = OrdemServicoMapper.class.getDeclaredConstructor();
        constructor.setAccessible(true);
        Executable executable = () -> constructor.newInstance();
        assertDoesNotThrow(executable);
    }
}
