package com.f1rsters.os_service.dto;

import com.f1rsters.os_service.domain.StatusOrdemServico;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AtualizarStatusOSDTO {
    @NotNull(message = "Novo status é obrigatório")
    public StatusOrdemServico novoStatus;
}
