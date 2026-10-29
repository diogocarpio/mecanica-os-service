package com.f1rsters.os_service.dto;

import com.f1rsters.os_service.domain.StatusOrdemServico;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
public class OSEvent {
    private Long osId;
    private String clienteId;
    private String veiculoId;
    private List<String> servicosIds;
    private List<String> pecasIds;
    private BigDecimal valorTotal;
    private StatusOrdemServico status;
    private String eventType;
    private LocalDateTime timestamp;

    public OSEvent(Long osId, String clienteId, String veiculoId, List<String> servicosIds,
                   List<String> pecasIds, BigDecimal valorTotal, StatusOrdemServico status, String eventType) {
        this.osId = osId;
        this.clienteId = clienteId;
        this.veiculoId = veiculoId;
        this.servicosIds = servicosIds;
        this.pecasIds = pecasIds;
        this.valorTotal = valorTotal;
        this.status = status;
        this.eventType = eventType;
        this.timestamp = LocalDateTime.now();
    }
}
