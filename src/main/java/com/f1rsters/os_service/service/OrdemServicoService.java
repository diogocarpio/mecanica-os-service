package com.f1rsters.os_service.service;

import com.f1rsters.os_service.domain.OrdemServico;
import com.f1rsters.os_service.domain.StatusOrdemServico;
import com.f1rsters.os_service.dto.*;
import com.f1rsters.os_service.repository.OrdemServicoRepository;
import com.f1rsters.os_service.util.InputNormalizer;
import com.f1rsters.os_service.util.SensitiveDataMasker;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class OrdemServicoService {
    private static final Logger log = LoggerFactory.getLogger(OrdemServicoService.class);

    private static final String OS_NAO_ENCONTRADA = "OS não encontrada";
    private static final String OPERATION = "operation";
    private static final String ERROR_REASON = "error_reason";
    private static final String ERROR_TYPE = "error_type";
    private static final String ERROR_MESSAGE = "error_message";
    private static final String OS_ID = "os_id";
    private static final String TIMER_DURATION_DESCRIPTION = "Duração de processamento das operações de OS";
    private static final String TIMER_NAME = "ordem_servico.processing.duration";
    private static final String CRIAR_ORDEM = "criar_ordem";
    private static final String ATUALIZAR_STATUS = "atualizar_status";
    private static final String PROCESSAR_ORCAMENTO = "processar_orcamento";
    private static final String PROCESSAR_NOTIFICACAO_STATUS = "processar_notificacao_status";
    private static final String OS_NAO_ENCONTRADA_REASON = "os_nao_encontrada";
    private static final String DETALHAR = "detalhar";
    private static final String CONSULTAR_STATUS = "consultar_status";

    private final OrdemServicoRepository repo;
    private final MeterRegistry meterRegistry;
    private final RabbitTemplate rabbitTemplate;

    private final Counter criarOrdemSuccessCounter;
    private final Counter criarOrdemErrorCounter;
    private final Counter atualizarStatusSuccessCounter;
    private final Counter atualizarStatusErrorCounter;
    private final Counter processarOrcamentoSuccessCounter;
    private final Counter processarOrcamentoErrorCounter;
    private final Counter processarNotificacaoStatusSuccessCounter;
    private final Counter processarNotificacaoStatusErrorCounter;
    private final Counter osCreatedTotalCounter;
    private final Counter statusTransitionCounter;

    public OrdemServicoService(OrdemServicoRepository repo,
                               MeterRegistry meterRegistry,
                               RabbitTemplate rabbitTemplate) {
        this.repo = repo;
        this.meterRegistry = meterRegistry;
        this.rabbitTemplate = rabbitTemplate;

        this.criarOrdemSuccessCounter = Counter.builder("ordem_servico.criar_ordem.success.total")
                .description("Sucesso na criação de ordens de serviço")
                .register(meterRegistry);
        this.criarOrdemErrorCounter = Counter.builder("ordem_servico.criar_ordem.unexpected_error.total")
                .description("Erros não esperados na criação de ordens de serviço")
                .register(meterRegistry);
        this.atualizarStatusSuccessCounter = Counter.builder("ordem_servico.atualizar_status.success.total")
                .description("Sucesso na atualização de status de ordens de serviço")
                .register(meterRegistry);
        this.atualizarStatusErrorCounter = Counter.builder("ordem_servico.atualizar_status.unexpected_error.total")
                .description("Erros não esperados na atualização de status")
                .register(meterRegistry);
        this.processarOrcamentoSuccessCounter = Counter.builder("ordem_servico.processar_orcamento.success.total")
                .description("Sucesso no processamento de orçamento")
                .register(meterRegistry);
        this.processarOrcamentoErrorCounter = Counter.builder("ordem_servico.processar_orcamento.unexpected_error.total")
                .description("Erros não esperados no processamento de orçamento")
                .register(meterRegistry);
        this.processarNotificacaoStatusSuccessCounter = Counter.builder("ordem_servico.processar_notificacao_status.success.total")
                .description("Sucesso no processamento de notificação de status")
                .register(meterRegistry);
        this.processarNotificacaoStatusErrorCounter = Counter.builder("ordem_servico.processar_notificacao_status.unexpected_error.total")
                .description("Erros não esperados no processamento de notificação de status")
                .register(meterRegistry);
        this.osCreatedTotalCounter = Counter.builder("ordem_servico.created.total")
                .description("Total de ordens de serviço criadas")
                .register(meterRegistry);
        this.statusTransitionCounter = Counter.builder("ordem_servico.status.transition.total")
                .description("Total de transições de status de ordens de serviço")
                .register(meterRegistry);
    }

    @Transactional
    public OrdemServico criarOrdem(CriarOrdemServicoDTO dto) {
        Timer.Sample sample = Timer.start(meterRegistry);
        MDC.put(OPERATION, CRIAR_ORDEM);

        try {
            log.info("Iniciando criação de ordem de serviço");

            String cpfCnpjNormalizado = InputNormalizer.normalizeCpfCnpj(dto.cpfCnpjCliente);
            String placaNormalizada = InputNormalizer.normalizePlaca(dto.placaVeiculo);

            // No microserviço, não acessamos diretamente os bancos de outros serviços
            // Usamos IDs externos e publicamos eventos para integração
            String clienteId = cpfCnpjNormalizado; // Simplificado - em produção seria lookup via API
            String veiculoId = placaNormalizada;   // Simplificado - em produção seria lookup via API

            // Calcular valor total (simplificado - em produção viria do Billing Service)
            BigDecimal valorTotal = BigDecimal.ZERO;

            // Construir OS
            OrdemServico os = new OrdemServico();
            os.setClienteId(clienteId);
            os.setVeiculoId(veiculoId);
            os.setServicosIds(dto.servicosIds);
            os.setPecasIds(dto.pecasIds);
            os.setValorTotal(valorTotal);
            os.setStatus(StatusOrdemServico.RECEBIDA);
            OrdemServico created = repo.save(os);

            MDC.put(OS_ID, created.getId().toString());
            MDC.put("os_status", created.getStatus().name());

            log.info("Ordem de serviço criada com sucesso: id={}, status={}", created.getId(), created.getStatus());

            // Publicar evento para o Billing Service gerar orçamento
            publishOSEvent(created, "os.created");

            osCreatedTotalCounter.increment();
            criarOrdemSuccessCounter.increment();

            sample.stop(Timer.builder(TIMER_NAME)
                    .description(TIMER_DURATION_DESCRIPTION)
                    .tag(OPERATION, CRIAR_ORDEM)
                    .register(meterRegistry));

            return created;

        } catch (Exception e) {
            MDC.put(ERROR_TYPE, e.getClass().getSimpleName());
            MDC.put(ERROR_MESSAGE, e.getMessage());
            log.error("Erro não tratado ao criar ordem de serviço", e);

            recordFailure(CRIAR_ORDEM, e.getClass().getSimpleName(), null);
            criarOrdemErrorCounter.increment();

            throw new RuntimeException("Erro ao criar ordem de serviço", e);

        } finally {
            MDC.clear();
        }
    }

    public OrdemServico atualizarStatus(Long id, StatusOrdemServico novoStatus) {
        Timer.Sample sample = Timer.start(meterRegistry);
        MDC.put(OPERATION, ATUALIZAR_STATUS);
        MDC.put(OS_ID, id.toString());
        MDC.put("novo_status", novoStatus.name());

        try {
            log.info("Atualizando status da OS: id={}, novoStatus={}", id, novoStatus);

            OrdemServico os = repo.findById(id).orElseThrow(() -> {
                MDC.put(ERROR_REASON, OS_NAO_ENCONTRADA_REASON);
                log.error("OS não encontrada: id={}", id);
                return new RuntimeException(OS_NAO_ENCONTRADA);
            });

            StatusOrdemServico statusAnterior = os.getStatus();
            MDC.put("status_anterior", statusAnterior.name());

            validarTransicaoStatus(statusAnterior, novoStatus, id);
            os.setStatus(novoStatus);
            OrdemServico updated = repo.save(os);

            log.info("Status atualizado com sucesso: id={}, de={}, para={}", id, statusAnterior, novoStatus);

            recordStatusTransition(updated, statusAnterior, novoStatus);

            atualizarStatusSuccessCounter.increment();

            sample.stop(Timer.builder(TIMER_NAME)
                    .description(TIMER_DURATION_DESCRIPTION)
                    .tag(OPERATION, ATUALIZAR_STATUS)
                    .register(meterRegistry));

            return updated;

        } catch (Exception e) {
            MDC.put(ERROR_TYPE, e.getClass().getSimpleName());
            MDC.put(ERROR_MESSAGE, e.getMessage());
            log.error("Erro não tratado ao atualizar status da OS", e);

            recordFailure(ATUALIZAR_STATUS, e.getClass().getSimpleName(), id);
            atualizarStatusErrorCounter.increment();

            throw new RuntimeException("Erro ao atualizar status da OS", e);

        } finally {
            MDC.clear();
        }
    }

    public List<OrdemServico> listarTodas() {
        return repo.findAllActiveOrderByStatusAndDate();
    }

    public OrdemServico detalhar(Long id) {
        MDC.put(OPERATION, DETALHAR);
        MDC.put(OS_ID, id.toString());

        try {
            log.info("Buscando detalhes da OS: id={}", id);

            OrdemServico os = repo.findById(id).orElseThrow(() -> {
                MDC.put(ERROR_REASON, OS_NAO_ENCONTRADA_REASON);
                log.error("OS não encontrada: id={}", id);
                return new RuntimeException(OS_NAO_ENCONTRADA);
            });

            log.info("OS encontrada: id={}, status={}", id, os.getStatus());

            return os;

        } finally {
            MDC.clear();
        }
    }

    public StatusOrdemServicoDTO consultarStatus(Long id) {
        MDC.put(OPERATION, CONSULTAR_STATUS);
        MDC.put(OS_ID, id.toString());

        try {
            log.info("Consultando status da OS: id={}", id);

            OrdemServico os = repo.findById(id).orElseThrow(() -> {
                MDC.put(ERROR_REASON, OS_NAO_ENCONTRADA_REASON);
                log.error("OS não encontrada: id={}", id);
                return new RuntimeException(OS_NAO_ENCONTRADA);
            });

            log.info("Status consultado: id={}, status={}", id, os.getStatus());

            return StatusOrdemServicoDTO.from(os.getId(), os.getStatus(), os.getCriadoEm());

        } finally {
            MDC.clear();
        }
    }

    @Transactional
    public OrdemServico processarRespostaOrcamento(Long id, RespostaOrcamentoDTO dto) {
        Timer.Sample sample = Timer.start(meterRegistry);
        MDC.put(OPERATION, PROCESSAR_ORCAMENTO);
        MDC.put(OS_ID, id.toString());
        MDC.put("aprovado", String.valueOf(dto.aprovado));

        try {
            log.info("Processando resposta de orçamento: id={}, aprovado={}", id, dto.aprovado);

            OrdemServico os = repo.findById(id).orElseThrow(() -> {
                MDC.put(ERROR_REASON, OS_NAO_ENCONTRADA_REASON);
                log.error("OS não encontrada: id={}", id);
                return new RuntimeException(OS_NAO_ENCONTRADA);
            });

            MDC.put("status_atual", os.getStatus().name());

            if (os.getStatus() != StatusOrdemServico.AGUARDANDO_APROVACAO) {
                MDC.put(ERROR_REASON, "transicao_status_invalida");
                log.error("OS não está aguardando aprovação: id={}, status={}", id, os.getStatus());
                throw new RuntimeException("OS não está aguardando aprovação");
            }

            StatusOrdemServico statusAnterior = os.getStatus();
            if (dto.aprovado) {
                os.setStatus(StatusOrdemServico.EM_EXECUCAO);
                log.info("Orçamento aprovado: id={}, novoStatus=EM_EXECUCAO", id);

                // Publicar evento para o Execution Service
                publishOSEvent(os, "os.approved");
            } else {
                log.info("Orçamento recusado: id={}, status mantido=AGUARDANDO_APROVACAO", id);
            }

            OrdemServico updated = repo.save(os);

            recordStatusTransition(updated, statusAnterior, updated.getStatus());

            processarOrcamentoSuccessCounter.increment();

            sample.stop(Timer.builder(TIMER_NAME)
                    .description(TIMER_DURATION_DESCRIPTION)
                    .tag(OPERATION, PROCESSAR_ORCAMENTO)
                    .register(meterRegistry));

            return updated;

        } catch (Exception e) {
            MDC.put(ERROR_TYPE, e.getClass().getSimpleName());
            MDC.put(ERROR_MESSAGE, e.getMessage());
            log.error("Erro não tratado ao processar resposta de orçamento", e);

            recordFailure(PROCESSAR_ORCAMENTO, e.getClass().getSimpleName(), id);
            processarOrcamentoErrorCounter.increment();

            throw new RuntimeException("Erro ao processar resposta de orçamento", e);

        } finally {
            MDC.clear();
        }
    }

    @Transactional
    public OrdemServico processarNotificacaoStatus(Long id, NotificacaoStatusDTO dto) {
        Timer.Sample sample = Timer.start(meterRegistry);
        MDC.put(OPERATION, PROCESSAR_NOTIFICACAO_STATUS);
        MDC.put(OS_ID, id.toString());
        MDC.put("novo_status", dto.novoStatus.name());

        try {
            log.info("Processando notificação de status: id={}, novoStatus={}", id, dto.novoStatus);

            OrdemServico os = repo.findById(id).orElseThrow(() -> {
                MDC.put(ERROR_REASON, OS_NAO_ENCONTRADA_REASON);
                log.error("OS não encontrada: id={}", id);
                return new RuntimeException(OS_NAO_ENCONTRADA);
            });

            StatusOrdemServico statusAnterior = os.getStatus();
            MDC.put("status_anterior", statusAnterior.name());

            os.setStatus(dto.novoStatus);
            OrdemServico updated = repo.save(os);

            log.info("Status atualizado via notificação: id={}, de={}, para={}", id, statusAnterior, dto.novoStatus);

            recordStatusTransition(updated, statusAnterior, dto.novoStatus);

            processarNotificacaoStatusSuccessCounter.increment();

            sample.stop(Timer.builder(TIMER_NAME)
                    .description(TIMER_DURATION_DESCRIPTION)
                    .tag(OPERATION, PROCESSAR_NOTIFICACAO_STATUS)
                    .register(meterRegistry));

            return updated;

        } catch (Exception e) {
            MDC.put(ERROR_TYPE, e.getClass().getSimpleName());
            MDC.put(ERROR_MESSAGE, e.getMessage());
            log.error("Erro não tratado ao processar notificação de status", e);

            recordFailure(PROCESSAR_NOTIFICACAO_STATUS, e.getClass().getSimpleName(), id);
            processarNotificacaoStatusErrorCounter.increment();

            throw new RuntimeException("Erro ao processar notificação de status", e);

        } finally {
            MDC.clear();
        }
    }

    @Transactional
    public OrdemServico cancelarOrdem(Long id, String motivo) {
        MDC.put(OPERATION, "cancelar_ordem");
        MDC.put(OS_ID, id.toString());

        try {
            OrdemServico os = repo.findById(id).orElseThrow(() -> new RuntimeException(OS_NAO_ENCONTRADA));
            StatusOrdemServico statusAnterior = os.getStatus();
            validarTransicaoStatus(statusAnterior, StatusOrdemServico.CANCELADA, id);

            os.setStatus(StatusOrdemServico.CANCELADA);
            OrdemServico updated = repo.save(os);
            publishOSEvent(updated, "os.cancelled");

            recordStatusTransition(updated, statusAnterior, StatusOrdemServico.CANCELADA);
            log.info("OS cancelada com sucesso: id={}, motivo={}", id, motivo);
            return updated;
        } finally {
            MDC.clear();
        }
    }

    @Transactional
    public OrdemServico processarEventoSaga(OSEvent event) {
        if (event == null || event.getOsId() == null) {
            throw new IllegalArgumentException("Evento de saga inválido");
        }

        OrdemServico os = repo.findById(event.getOsId()).orElseThrow(() -> new RuntimeException(OS_NAO_ENCONTRADA));
        StatusOrdemServico statusAnterior = os.getStatus();

        switch (event.getEventType()) {
            case "orcamento.aprovado" -> {
                validarTransicaoStatus(statusAnterior, StatusOrdemServico.EM_EXECUCAO, event.getOsId());
                os.setStatus(StatusOrdemServico.EM_EXECUCAO);
                publishOSEvent(os, "os.approved");
            }
            case "orcamento.rejeitado" -> {
                validarTransicaoStatus(statusAnterior, StatusOrdemServico.CANCELADA, event.getOsId());
                os.setStatus(StatusOrdemServico.CANCELADA);
                publishOSEvent(os, "os.cancelled");
            }
            case "execution.completed" -> {
                validarTransicaoStatus(statusAnterior, StatusOrdemServico.FINALIZADA, event.getOsId());
                os.setStatus(StatusOrdemServico.FINALIZADA);
                publishOSEvent(os, "os.status.updated");
            }
            case "os.cancelled" -> {
                validarTransicaoStatus(statusAnterior, StatusOrdemServico.CANCELADA, event.getOsId());
                os.setStatus(StatusOrdemServico.CANCELADA);
            }
            default -> log.warn("Evento de saga não mapeado: {}", event.getEventType());
        }

        OrdemServico updated = repo.save(os);
        recordStatusTransition(updated, statusAnterior, updated.getStatus());
        return updated;
    }

    private void validarTransicaoStatus(StatusOrdemServico statusAnterior, StatusOrdemServico novoStatus, Long id) {
        if (statusAnterior == null) {
            throw new IllegalStateException("Status atual da OS não pode ser nulo");
        }

        boolean transicaoValida = switch (statusAnterior) {
            case RECEBIDA -> novoStatus == StatusOrdemServico.DIAGNOSTICO || novoStatus == StatusOrdemServico.AGUARDANDO_APROVACAO || novoStatus == StatusOrdemServico.CANCELADA;
            case DIAGNOSTICO -> novoStatus == StatusOrdemServico.AGUARDANDO_APROVACAO || novoStatus == StatusOrdemServico.CANCELADA;
            case AGUARDANDO_APROVACAO -> novoStatus == StatusOrdemServico.EM_EXECUCAO || novoStatus == StatusOrdemServico.CANCELADA;
            case EM_EXECUCAO -> novoStatus == StatusOrdemServico.FINALIZADA || novoStatus == StatusOrdemServico.CANCELADA;
            case FINALIZADA -> novoStatus == StatusOrdemServico.ENTREGUE || novoStatus == StatusOrdemServico.CANCELADA;
            case ENTREGUE -> false;
            case CANCELADA -> false;
        };

        if (!transicaoValida) {
            throw new IllegalStateException("Transição de status inválida para a OS " + id + ": " + statusAnterior + " -> " + novoStatus);
        }
    }

    private void publishOSEvent(OrdemServico os, String eventType) {
        try {
            OSEvent event = new OSEvent(
                os.getId(),
                os.getClienteId(),
                os.getVeiculoId(),
                os.getServicosIds(),
                os.getPecasIds(),
                os.getValorTotal(),
                os.getStatus(),
                eventType
            );
            rabbitTemplate.convertAndSend("os.exchange", eventType, event);
            log.info("Evento publicado: eventType={}, osId={}", eventType, os.getId());
        } catch (Exception e) {
            log.error("Erro ao publicar evento: eventType={}, osId={}", eventType, os.getId(), e);
        }
    }

    private void recordFailure(String operation, String errorType, Long osId) {
        Counter.builder("ordem_servico.processing.failure.total")
                .description("Falhas no processamento de ordens de serviço")
                .tag(OPERATION, operation)
                .tag(ERROR_TYPE, errorType)
                .tag(OS_ID, osId != null ? osId.toString() : "unknown")
                .register(meterRegistry)
                .increment();
    }

    private void recordStatusTransition(OrdemServico os,
                                        StatusOrdemServico statusAnterior,
                                        StatusOrdemServico statusAtual) {
        Counter.builder("ordem_servico.status.transition.total")
                .description("Total de transições de status de ordens de serviço")
                .tag("from", statusAnterior.name())
                .tag("to", statusAtual.name())
                .register(meterRegistry)
                .increment();
    }
}
