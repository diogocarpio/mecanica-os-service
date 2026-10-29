package com.f1rsters.os_service.domain;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "ordens_servico")
public class OrdemServico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String clienteId; // ID do cliente (referência externa)

    @Column(nullable = false)
    private String veiculoId; // ID do veículo (referência externa)

    @ElementCollection
    @CollectionTable(name = "os_servicos", joinColumns = @JoinColumn(name = "os_id"))
    @Column(name = "servico_id")
    private List<String> servicosIds; // IDs dos serviços (referência externa)

    @ElementCollection
    @CollectionTable(name = "os_pecas", joinColumns = @JoinColumn(name = "os_id"))
    @Column(name = "peca_id")
    private List<String> pecasIds; // IDs das peças (referência externa)

    private BigDecimal valorTotal;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusOrdemServico status;

    @Column(nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    private LocalDateTime atualizadoEm;

    private LocalDateTime deletedAt;

    @PrePersist
    protected void onCreate() {
        criadoEm = LocalDateTime.now();
        atualizadoEm = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        atualizadoEm = LocalDateTime.now();
    }
}
