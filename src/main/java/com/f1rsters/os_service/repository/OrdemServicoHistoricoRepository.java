package com.f1rsters.os_service.repository;

import com.f1rsters.os_service.domain.OrdemServicoHistorico;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrdemServicoHistoricoRepository extends JpaRepository<OrdemServicoHistorico, Long> {
    List<OrdemServicoHistorico> findByOsIdOrderByCriadoEmAsc(Long osId);
}
