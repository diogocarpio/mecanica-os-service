package com.f1rsters.os_service.dto;

import com.f1rsters.os_service.domain.StatusOrdemServico;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class OrdemServicoDTO {
    public Long id;
    public String clienteId;
    public String veiculoId;
    public List<String> servicosIds;
    public List<String> pecasIds;
    public BigDecimal valorTotal;
    public StatusOrdemServico status;
    public LocalDateTime criadoEm;
    public LocalDateTime atualizadoEm;
}
