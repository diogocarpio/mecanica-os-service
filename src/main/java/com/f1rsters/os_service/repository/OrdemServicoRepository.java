package com.f1rsters.os_service.repository;

import com.f1rsters.os_service.domain.OrdemServico;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface OrdemServicoRepository extends JpaRepository<OrdemServico, Long> {

    @Query("SELECT os FROM OrdemServico os WHERE os.deletedAt IS NULL AND os.status NOT IN ('FINALIZADA', 'ENTREGUE', 'CANCELADA') ORDER BY CASE os.status WHEN 'EM_EXECUCAO' THEN 1 WHEN 'AGUARDANDO_APROVACAO' THEN 2 WHEN 'DIAGNOSTICO' THEN 3 WHEN 'RECEBIDA' THEN 4 ELSE 5 END, os.criadoEm ASC")
    List<OrdemServico> findAllActiveOrderByStatusAndDate();

    Optional<OrdemServico> findByClienteIdAndDeletedAtIsNull(String clienteId);

    List<OrdemServico> findByClienteIdAndDeletedAtIsNullOrderByCriadoEmDesc(String clienteId);
}
