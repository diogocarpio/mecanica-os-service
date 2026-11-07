package com.f1rsters.os_service.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

@Entity
@Table(name = "ordem_servico_historico")
@Data
public class OrdemServicoHistorico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "os_id", nullable = false)
    private Long osId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_anterior")
    private StatusOrdemServico statusAnterior;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_novo", nullable = false)
    private StatusOrdemServico statusNovo;

    @Column(nullable = false)
    private String origem;

    @Column
    private String motivo;

    @Column(name = "criado_em", nullable = false)
    private LocalDateTime criadoEm;

    @PrePersist
    void onCreate() {
        this.criadoEm = LocalDateTime.now();
    }
}
