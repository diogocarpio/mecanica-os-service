package com.f1rsters.os_service.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class RespostaOrcamentoDTO {
    @NotNull(message = "Campo aprovado é obrigatório")
    public boolean aprovado;
}
