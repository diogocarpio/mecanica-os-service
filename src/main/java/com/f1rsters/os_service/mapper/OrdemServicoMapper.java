package com.f1rsters.os_service.mapper;

import com.f1rsters.os_service.domain.OrdemServico;
import com.f1rsters.os_service.dto.OrdemServicoDTO;
import com.f1rsters.os_service.dto.StatusOrdemServicoDTO;

public final class OrdemServicoMapper {

    private OrdemServicoMapper() {
    }

    public static OrdemServicoDTO toDTO(OrdemServico ordemServico) {
        if (ordemServico == null) {
            return null;
        }

        OrdemServicoDTO dto = new OrdemServicoDTO();
        dto.id = ordemServico.getId();
        dto.clienteId = ordemServico.getClienteId();
        dto.veiculoId = ordemServico.getVeiculoId();
        dto.servicosIds = ordemServico.getServicosIds();
        dto.pecasIds = ordemServico.getPecasIds();
        dto.valorTotal = ordemServico.getValorTotal();
        dto.status = ordemServico.getStatus();
        dto.criadoEm = ordemServico.getCriadoEm();
        dto.atualizadoEm = ordemServico.getAtualizadoEm();
        return dto;
    }

    public static StatusOrdemServicoDTO toStatusDTO(OrdemServico ordemServico) {
        if (ordemServico == null) {
            return null;
        }

        return StatusOrdemServicoDTO.from(
                ordemServico.getId(),
                ordemServico.getStatus(),
                ordemServico.getCriadoEm()
        );
    }
}
