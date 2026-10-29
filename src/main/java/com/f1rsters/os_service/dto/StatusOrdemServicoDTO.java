package com.f1rsters.os_service.dto;

import com.f1rsters.os_service.domain.StatusOrdemServico;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class StatusOrdemServicoDTO {
    public Long osId;
    public StatusOrdemServico status;
    public LocalDateTime criadoEm;

    public static StatusOrdemServicoDTO from(Long osId, StatusOrdemServico status, LocalDateTime criadoEm) {
        StatusOrdemServicoDTO dto = new StatusOrdemServicoDTO();
        dto.osId = osId;
        dto.status = status;
        dto.criadoEm = criadoEm;
        return dto;
    }
}
