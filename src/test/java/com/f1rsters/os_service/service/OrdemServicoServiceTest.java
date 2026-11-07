package com.f1rsters.os_service.service;

import com.f1rsters.os_service.domain.OrdemServico;
import com.f1rsters.os_service.domain.OrdemServicoHistorico;
import com.f1rsters.os_service.domain.StatusOrdemServico;
import com.f1rsters.os_service.dto.CriarOrdemServicoDTO;
import com.f1rsters.os_service.dto.NotificacaoStatusDTO;
import com.f1rsters.os_service.dto.OSEvent;
import com.f1rsters.os_service.dto.RespostaOrcamentoDTO;
import com.f1rsters.os_service.repository.OrdemServicoHistoricoRepository;
import com.f1rsters.os_service.repository.OrdemServicoRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrdemServicoServiceTest {

    @Mock
    private OrdemServicoRepository repository;

    @Mock
    private OrdemServicoHistoricoRepository historicoRepository;

    @Mock
    private RabbitTemplate rabbitTemplate;

    private MeterRegistry meterRegistry;
    private OrdemServicoService service;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        service = new OrdemServicoService(repository, historicoRepository, meterRegistry, rabbitTemplate);
    }

    @Test
    void criarOrdem_DeveRetornarOrdemCriada() {
        CriarOrdemServicoDTO dto = new CriarOrdemServicoDTO();
        dto.cpfCnpjCliente = "12345678909";
        dto.placaVeiculo = "ABC1234";
        dto.servicosIds = List.of("1", "2");
        dto.pecasIds = List.of("1");

        OrdemServico os = new OrdemServico();
        os.setId(1L);
        os.setClienteId("12345678909");
        os.setVeiculoId("ABC1234");
        os.setServicosIds(dto.servicosIds);
        os.setPecasIds(dto.pecasIds);
        os.setStatus(StatusOrdemServico.RECEBIDA);

        when(repository.save(any(OrdemServico.class))).thenReturn(os);

        OrdemServico result = service.criarOrdem(dto);

        assertNotNull(result);
        assertEquals(StatusOrdemServico.RECEBIDA, result.getStatus());
        verify(repository, times(1)).save(any(OrdemServico.class));
        verify(rabbitTemplate, times(1)).convertAndSend(eq("os.exchange"), eq("os.created"), any(OSEvent.class));
    }

    @Test
    void criarOrdem_DeveLancarErroQuandoRepositorioFalhar() {
        CriarOrdemServicoDTO dto = new CriarOrdemServicoDTO();
        dto.cpfCnpjCliente = "12345678909";
        dto.placaVeiculo = "ABC1234";
        dto.servicosIds = List.of("1");

        when(repository.save(any(OrdemServico.class))).thenThrow(new RuntimeException("db fail"));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.criarOrdem(dto));
        assertEquals("Erro ao criar ordem de serviço", ex.getMessage());
        assertNotNull(ex.getCause());
        assertEquals("db fail", ex.getCause().getMessage());
    }

    @Test
    void criarOrdem_DeveIgnorarFalhaAoPublicarEvento() {
        CriarOrdemServicoDTO dto = new CriarOrdemServicoDTO();
        dto.cpfCnpjCliente = "12345678909";
        dto.placaVeiculo = "ABC1234";
        dto.servicosIds = List.of("1");

        OrdemServico os = new OrdemServico();
        os.setId(1L);
        os.setClienteId("12345678909");
        os.setVeiculoId("ABC1234");
        os.setServicosIds(dto.servicosIds);
        os.setStatus(StatusOrdemServico.RECEBIDA);

        when(repository.save(any(OrdemServico.class))).thenReturn(os);
        doThrow(new RuntimeException("amqp fail")).when(rabbitTemplate).convertAndSend(eq("os.exchange"), eq("os.created"), any(OSEvent.class));

        OrdemServico result = service.criarOrdem(dto);

        assertNotNull(result);
        assertEquals(StatusOrdemServico.RECEBIDA, result.getStatus());
    }

    @Test
    void atualizarStatus_DeveAtualizarStatusComSucesso() {
        Long id = 1L;
        StatusOrdemServico novoStatus = StatusOrdemServico.AGUARDANDO_APROVACAO;

        OrdemServico os = new OrdemServico();
        os.setId(id);
        os.setStatus(StatusOrdemServico.RECEBIDA);

        when(repository.findById(id)).thenReturn(Optional.of(os));
        when(repository.save(any(OrdemServico.class))).thenReturn(os);

        OrdemServico result = service.atualizarStatus(id, novoStatus);

        assertNotNull(result);
        assertEquals(novoStatus, result.getStatus());
        verify(repository, times(1)).findById(id);
        verify(repository, times(1)).save(any(OrdemServico.class));
    }

    @Test
    void atualizarStatus_DeveLancarErroQuandoOrdemNaoExiste() {
        Long id = 99L;

        when(repository.findById(id)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.atualizarStatus(id, StatusOrdemServico.EM_EXECUCAO));
        assertEquals("Erro ao atualizar status da OS", ex.getMessage());
        assertNotNull(ex.getCause());
        assertEquals("OS não encontrada", ex.getCause().getMessage());
    }

    @Test
    void listarTodas_DeveRetornarListaDeOrdens() {
        OrdemServico os1 = new OrdemServico();
        os1.setId(1L);
        os1.setStatus(StatusOrdemServico.RECEBIDA);

        OrdemServico os2 = new OrdemServico();
        os2.setId(2L);
        os2.setStatus(StatusOrdemServico.EM_EXECUCAO);

        when(repository.findAllActiveOrderByStatusAndDate()).thenReturn(List.of(os1, os2));

        List<OrdemServico> result = service.listarTodas();

        assertNotNull(result);
        assertEquals(2, result.size());
        verify(repository, times(1)).findAllActiveOrderByStatusAndDate();
    }

    @Test
    void detalhar_DeveRetornarOrdemQuandoEncontrada() {
        Long id = 1L;
        OrdemServico os = new OrdemServico();
        os.setId(id);
        os.setStatus(StatusOrdemServico.RECEBIDA);

        when(repository.findById(id)).thenReturn(Optional.of(os));

        OrdemServico result = service.detalhar(id);

        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(repository, times(1)).findById(id);
    }

    @Test
    void detalhar_DeveLancarQuandoOrdemNaoExiste() {
        Long id = 99L;
        when(repository.findById(id)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.detalhar(id));
        assertEquals("OS não encontrada", ex.getMessage());
    }

    @Test
    void consultarStatus_DeveRetornarStatusDTO() {
        Long id = 1L;
        OrdemServico os = new OrdemServico();
        os.setId(id);
        os.setStatus(StatusOrdemServico.RECEBIDA);

        when(repository.findById(id)).thenReturn(Optional.of(os));

        var result = service.consultarStatus(id);

        assertNotNull(result);
        assertEquals(id, result.osId);
        assertEquals(StatusOrdemServico.RECEBIDA, result.status);
        verify(repository, times(1)).findById(id);
    }

    @Test
    void consultarStatus_DeveLancarQuandoOrdemNaoExiste() {
        Long id = 99L;
        when(repository.findById(id)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.consultarStatus(id));
        assertEquals("OS não encontrada", ex.getMessage());
    }

    @Test
    void processarRespostaOrcamento_DeveAtualizarParaEmExecucaoQuandoAprovado() {
        Long id = 1L;
        RespostaOrcamentoDTO dto = new RespostaOrcamentoDTO();
        dto.aprovado = true;

        OrdemServico os = new OrdemServico();
        os.setId(id);
        os.setStatus(StatusOrdemServico.AGUARDANDO_APROVACAO);

        when(repository.findById(id)).thenReturn(Optional.of(os));
        when(repository.save(any(OrdemServico.class))).thenReturn(os);

        OrdemServico result = service.processarRespostaOrcamento(id, dto);

        assertNotNull(result);
        assertEquals(StatusOrdemServico.EM_EXECUCAO, result.getStatus());
        verify(repository, times(1)).findById(id);
        verify(repository, times(1)).save(any(OrdemServico.class));
        verify(rabbitTemplate, times(1)).convertAndSend(eq("os.exchange"), eq("os.approved"), any(OSEvent.class));
    }

    @Test
    void processarRespostaOrcamento_DeveManterStatusQuandoReprovado() {
        Long id = 1L;
        RespostaOrcamentoDTO dto = new RespostaOrcamentoDTO();
        dto.aprovado = false;

        OrdemServico os = new OrdemServico();
        os.setId(id);
        os.setStatus(StatusOrdemServico.AGUARDANDO_APROVACAO);

        when(repository.findById(id)).thenReturn(Optional.of(os));
        when(repository.save(any(OrdemServico.class))).thenReturn(os);

        OrdemServico result = service.processarRespostaOrcamento(id, dto);

        assertNotNull(result);
        assertEquals(StatusOrdemServico.AGUARDANDO_APROVACAO, result.getStatus());
        verify(repository, times(1)).findById(id);
        verify(repository, times(1)).save(any(OrdemServico.class));
    }

    @Test
    void processarRespostaOrcamento_DeveLancarQuandoStatusNaoPermiteTransicao() {
        Long id = 1L;
        RespostaOrcamentoDTO dto = new RespostaOrcamentoDTO();
        dto.aprovado = true;

        OrdemServico os = new OrdemServico();
        os.setId(id);
        os.setStatus(StatusOrdemServico.RECEBIDA);

        when(repository.findById(id)).thenReturn(Optional.of(os));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.processarRespostaOrcamento(id, dto));
        assertEquals("Erro ao processar resposta de orçamento", ex.getMessage());
        assertNotNull(ex.getCause());
        assertEquals("OS não está aguardando aprovação", ex.getCause().getMessage());
    }

    @Test
    void processarRespostaOrcamento_DeveLancarQuandoOrdemNaoExiste() {
        Long id = 99L;
        RespostaOrcamentoDTO dto = new RespostaOrcamentoDTO();
        dto.aprovado = true;

        when(repository.findById(id)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.processarRespostaOrcamento(id, dto));
        assertEquals("Erro ao processar resposta de orçamento", ex.getMessage());
        assertNotNull(ex.getCause());
        assertEquals("OS não encontrada", ex.getCause().getMessage());
    }

    @Test
    void processarNotificacaoStatus_DeveAtualizarStatus() {
        Long id = 1L;
        NotificacaoStatusDTO dto = new NotificacaoStatusDTO();
        dto.novoStatus = StatusOrdemServico.DIAGNOSTICO;

        OrdemServico os = new OrdemServico();
        os.setId(id);
        os.setStatus(StatusOrdemServico.RECEBIDA);

        when(repository.findById(id)).thenReturn(Optional.of(os));
        when(repository.save(any(OrdemServico.class))).thenReturn(os);

        OrdemServico result = service.processarNotificacaoStatus(id, dto);

        assertNotNull(result);
        assertEquals(StatusOrdemServico.DIAGNOSTICO, result.getStatus());
        verify(repository, times(1)).findById(id);
        verify(repository, times(1)).save(any(OrdemServico.class));
    }

    @Test
    void processarNotificacaoStatus_DeveLancarQuandoOrdemNaoExiste() {
        Long id = 99L;
        NotificacaoStatusDTO dto = new NotificacaoStatusDTO();
        dto.novoStatus = StatusOrdemServico.DIAGNOSTICO;

        when(repository.findById(id)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.processarNotificacaoStatus(id, dto));
        assertEquals("Erro ao processar notificação de status", ex.getMessage());
        assertNotNull(ex.getCause());
        assertEquals("OS não encontrada", ex.getCause().getMessage());
    }

    @Test
    void atualizarStatus_DeveRejeitarTransicaoInvalida() {
        Long id = 1L;

        OrdemServico os = new OrdemServico();
        os.setId(id);
        os.setStatus(StatusOrdemServico.RECEBIDA);

        when(repository.findById(id)).thenReturn(Optional.of(os));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.atualizarStatus(id, StatusOrdemServico.FINALIZADA));
        assertEquals("Erro ao atualizar status da OS", ex.getMessage());
        assertNotNull(ex.getCause());
        assertTrue(ex.getCause().getMessage().contains("Transição de status inválida"));
    }

    @Test
    void processarEventoSaga_DeveFinalizarOrdemQuandoExecutionCompleta() {
        Long id = 1L;
        OrdemServico os = new OrdemServico();
        os.setId(id);
        os.setStatus(StatusOrdemServico.EM_EXECUCAO);
        os.setClienteId("12345678909");
        os.setVeiculoId("ABC1234");

        when(repository.findById(id)).thenReturn(Optional.of(os));
        when(repository.save(any(OrdemServico.class))).thenReturn(os);

        OSEvent event = new OSEvent(id, "12345678909", "ABC1234", List.of("1"), List.of("A1"), null, StatusOrdemServico.FINALIZADA, "execution.completed");

        OrdemServico result = service.processarEventoSaga(event);

        assertNotNull(result);
        assertEquals(StatusOrdemServico.FINALIZADA, result.getStatus());
        verify(repository, times(1)).save(any(OrdemServico.class));
    }

    @Test
    void processarEventoSaga_DeveCancelarOrdemQuandoRecebeEventoCancelado() {
        Long id = 1L;
        OrdemServico os = new OrdemServico();
        os.setId(id);
        os.setStatus(StatusOrdemServico.RECEBIDA);
        os.setClienteId("12345678909");
        os.setVeiculoId("ABC1234");

        when(repository.findById(id)).thenReturn(Optional.of(os));
        when(repository.save(any(OrdemServico.class))).thenReturn(os);

        OSEvent event = new OSEvent(id, "12345678909", "ABC1234", List.of("1"), List.of("A1"), null, StatusOrdemServico.CANCELADA, "os.cancelled");

        OrdemServico result = service.processarEventoSaga(event);

        assertNotNull(result);
        assertEquals(StatusOrdemServico.CANCELADA, result.getStatus());
        verify(repository, times(1)).save(any(OrdemServico.class));
    }

    @Test
    void processarEventoSaga_DeveAprovarOrdemQuandoOrcamentoAprovado() {
        Long id = 1L;
        OrdemServico os = ordemComStatus(id, StatusOrdemServico.AGUARDANDO_APROVACAO);

        when(repository.findById(id)).thenReturn(Optional.of(os));
        when(repository.save(any(OrdemServico.class))).thenAnswer(inv -> inv.getArgument(0));

        OSEvent event = eventoSaga(id, StatusOrdemServico.EM_EXECUCAO, "orcamento.aprovado");

        OrdemServico result = service.processarEventoSaga(event);

        assertEquals(StatusOrdemServico.EM_EXECUCAO, result.getStatus());
        verify(rabbitTemplate, times(1)).convertAndSend(eq("os.exchange"), eq("os.approved"), any(OSEvent.class));
    }

    @Test
    void processarEventoSaga_DeveCancelarOrdemQuandoOrcamentoRejeitado() {
        Long id = 1L;
        OrdemServico os = ordemComStatus(id, StatusOrdemServico.AGUARDANDO_APROVACAO);

        when(repository.findById(id)).thenReturn(Optional.of(os));
        when(repository.save(any(OrdemServico.class))).thenAnswer(inv -> inv.getArgument(0));

        OSEvent event = eventoSaga(id, StatusOrdemServico.CANCELADA, "orcamento.rejeitado");

        OrdemServico result = service.processarEventoSaga(event);

        assertEquals(StatusOrdemServico.CANCELADA, result.getStatus());
        verify(rabbitTemplate, times(1)).convertAndSend(eq("os.exchange"), eq("os.cancelled"), any(OSEvent.class));
    }

    @Test
    void processarEventoSaga_DeveIgnorarEventoDesconhecido() {
        Long id = 1L;
        OrdemServico os = ordemComStatus(id, StatusOrdemServico.RECEBIDA);

        when(repository.findById(id)).thenReturn(Optional.of(os));
        when(repository.save(any(OrdemServico.class))).thenAnswer(inv -> inv.getArgument(0));

        OSEvent event = eventoSaga(id, StatusOrdemServico.RECEBIDA, "evento.desconhecido");

        OrdemServico result = service.processarEventoSaga(event);

        assertEquals(StatusOrdemServico.RECEBIDA, result.getStatus());
        verify(repository, times(1)).save(any(OrdemServico.class));
    }

    @Test
    void processarEventoSaga_DeveLancarQuandoEventoNulo() {
        assertThrows(IllegalArgumentException.class, () -> service.processarEventoSaga(null));
    }

    @Test
    void processarEventoSaga_DeveLancarQuandoOsIdNulo() {
        OSEvent event = eventoSaga(null, StatusOrdemServico.RECEBIDA, "os.created");

        assertThrows(IllegalArgumentException.class, () -> service.processarEventoSaga(event));
    }

    @Test
    void processarEventoSaga_DeveLancarQuandoOrdemNaoExiste() {
        Long id = 99L;
        when(repository.findById(id)).thenReturn(Optional.empty());

        OSEvent event = eventoSaga(id, StatusOrdemServico.RECEBIDA, "os.created");

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.processarEventoSaga(event));
        assertEquals("OS não encontrada", ex.getMessage());
    }

    @Test
    void processarEventoSaga_DeveLancarQuandoTransicaoInvalida() {
        Long id = 1L;
        OrdemServico os = ordemComStatus(id, StatusOrdemServico.RECEBIDA);

        when(repository.findById(id)).thenReturn(Optional.of(os));

        OSEvent event = eventoSaga(id, StatusOrdemServico.EM_EXECUCAO, "orcamento.aprovado");

        assertThrows(IllegalStateException.class, () -> service.processarEventoSaga(event));
        verify(repository, never()).save(any(OrdemServico.class));
    }

    @Test
    void cancelarOrdem_DeveCancelarComSucesso() {
        Long id = 1L;
        OrdemServico os = ordemComStatus(id, StatusOrdemServico.RECEBIDA);

        when(repository.findById(id)).thenReturn(Optional.of(os));
        when(repository.save(any(OrdemServico.class))).thenAnswer(inv -> inv.getArgument(0));

        OrdemServico result = service.cancelarOrdem(id, "Cliente desistiu");

        assertNotNull(result);
        assertEquals(StatusOrdemServico.CANCELADA, result.getStatus());
        verify(repository, times(1)).save(any(OrdemServico.class));
        verify(rabbitTemplate, times(1)).convertAndSend(eq("os.exchange"), eq("os.cancelled"), any(OSEvent.class));
    }

    @Test
    void cancelarOrdem_DeveLancarQuandoOrdemNaoExiste() {
        Long id = 99L;
        when(repository.findById(id)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.cancelarOrdem(id, "motivo"));
        assertEquals("OS não encontrada", ex.getMessage());
    }

    @Test
    void cancelarOrdem_DeveLancarQuandoTransicaoInvalida() {
        Long id = 1L;
        OrdemServico os = ordemComStatus(id, StatusOrdemServico.ENTREGUE);

        when(repository.findById(id)).thenReturn(Optional.of(os));

        assertThrows(IllegalStateException.class, () -> service.cancelarOrdem(id, "motivo"));
        verify(repository, never()).save(any(OrdemServico.class));
    }

    @ParameterizedTest(name = "transição válida {0} -> {1}")
    @CsvSource({
            "RECEBIDA, DIAGNOSTICO",
            "RECEBIDA, AGUARDANDO_APROVACAO",
            "RECEBIDA, CANCELADA",
            "DIAGNOSTICO, AGUARDANDO_APROVACAO",
            "DIAGNOSTICO, CANCELADA",
            "AGUARDANDO_APROVACAO, EM_EXECUCAO",
            "AGUARDANDO_APROVACAO, CANCELADA",
            "EM_EXECUCAO, FINALIZADA",
            "EM_EXECUCAO, CANCELADA",
            "FINALIZADA, ENTREGUE",
            "FINALIZADA, CANCELADA"
    })
    void atualizarStatus_DevePermitirTransicoesValidas(StatusOrdemServico atual, StatusOrdemServico novo) {
        Long id = 1L;
        OrdemServico os = ordemComStatus(id, atual);

        when(repository.findById(id)).thenReturn(Optional.of(os));
        when(repository.save(any(OrdemServico.class))).thenAnswer(inv -> inv.getArgument(0));

        OrdemServico result = service.atualizarStatus(id, novo);

        assertNotNull(result);
        assertEquals(novo, result.getStatus());
        verify(repository, times(1)).save(any(OrdemServico.class));
    }

    @ParameterizedTest(name = "transição inválida {0} -> {1}")
    @CsvSource({
            "RECEBIDA, EM_EXECUCAO",
            "RECEBIDA, FINALIZADA",
            "RECEBIDA, ENTREGUE",
            "DIAGNOSTICO, EM_EXECUCAO",
            "DIAGNOSTICO, FINALIZADA",
            "DIAGNOSTICO, RECEBIDA",
            "AGUARDANDO_APROVACAO, RECEBIDA",
            "AGUARDANDO_APROVACAO, FINALIZADA",
            "EM_EXECUCAO, RECEBIDA",
            "EM_EXECUCAO, AGUARDANDO_APROVACAO",
            "EM_EXECUCAO, DIAGNOSTICO",
            "FINALIZADA, EM_EXECUCAO",
            "FINALIZADA, RECEBIDA",
            "ENTREGUE, CANCELADA",
            "ENTREGUE, RECEBIDA",
            "CANCELADA, RECEBIDA",
            "CANCELADA, EM_EXECUCAO"
    })
    void atualizarStatus_DeveRejeitarTransicoesInvalidas(StatusOrdemServico atual, StatusOrdemServico novo) {
        Long id = 1L;
        OrdemServico os = ordemComStatus(id, atual);

        when(repository.findById(id)).thenReturn(Optional.of(os));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.atualizarStatus(id, novo));
        assertEquals("Erro ao atualizar status da OS", ex.getMessage());
        assertNotNull(ex.getCause());
        assertTrue(ex.getCause().getMessage().contains("Transição de status inválida"));
        verify(repository, never()).save(any(OrdemServico.class));
    }

    @Test
    void cancelarOrdem_DeveRejeitarStatusAtualNulo() {
        Long id = 1L;
        OrdemServico os = ordemComStatus(id, null);

        when(repository.findById(id)).thenReturn(Optional.of(os));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> service.cancelarOrdem(id, "motivo"));
        assertEquals("Status atual da OS não pode ser nulo", ex.getMessage());
    }

    @Test
    void listarHistorico_DeveRetornarHistoricoOrdenado() {
        Long id = 1L;
        OrdemServico os = ordemComStatus(id, StatusOrdemServico.RECEBIDA);
        OrdemServicoHistorico h1 = new OrdemServicoHistorico();
        h1.setOsId(id);
        h1.setStatusNovo(StatusOrdemServico.RECEBIDA);
        h1.setOrigem("CRIACAO");
        OrdemServicoHistorico h2 = new OrdemServicoHistorico();
        h2.setOsId(id);
        h2.setStatusAnterior(StatusOrdemServico.RECEBIDA);
        h2.setStatusNovo(StatusOrdemServico.DIAGNOSTICO);
        h2.setOrigem("ATUALIZACAO_MANUAL");

        when(repository.findById(id)).thenReturn(Optional.of(os));
        when(historicoRepository.findByOsIdOrderByCriadoEmAsc(id)).thenReturn(List.of(h1, h2));

        List<OrdemServicoHistorico> result = service.listarHistorico(id);

        assertEquals(2, result.size());
        assertEquals("CRIACAO", result.get(0).getOrigem());
        assertEquals("ATUALIZACAO_MANUAL", result.get(1).getOrigem());
        verify(historicoRepository).findByOsIdOrderByCriadoEmAsc(id);
    }

    @Test
    void listarHistorico_DeveLancarErroQuandoOsNaoExiste() {
        Long id = 99L;
        when(repository.findById(id)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> service.listarHistorico(id));
        assertEquals("OS não encontrada", ex.getMessage());
        verify(historicoRepository, never()).findByOsIdOrderByCriadoEmAsc(any());
    }

    @Test
    void criarOrdem_DeveRegistrarHistoricoDeCriacao() {
        CriarOrdemServicoDTO dto = new CriarOrdemServicoDTO();
        dto.cpfCnpjCliente = "12345678909";
        dto.placaVeiculo = "ABC1234";
        dto.servicosIds = List.of("1");

        OrdemServico os = ordemComStatus(1L, StatusOrdemServico.RECEBIDA);
        when(repository.save(any(OrdemServico.class))).thenReturn(os);

        service.criarOrdem(dto);

        verify(historicoRepository).save(argThat(h ->
                h.getOsId().equals(1L)
                        && h.getStatusAnterior() == null
                        && h.getStatusNovo() == StatusOrdemServico.RECEBIDA
                        && "CRIACAO".equals(h.getOrigem())));
    }

    @Test
    void atualizarStatus_DeveRegistrarHistoricoDaTransicao() {
        Long id = 1L;
        OrdemServico os = ordemComStatus(id, StatusOrdemServico.RECEBIDA);
        when(repository.findById(id)).thenReturn(Optional.of(os));
        when(repository.save(any(OrdemServico.class))).thenAnswer(inv -> inv.getArgument(0));

        service.atualizarStatus(id, StatusOrdemServico.DIAGNOSTICO);

        verify(historicoRepository).save(argThat(h ->
                h.getOsId().equals(id)
                        && h.getStatusAnterior() == StatusOrdemServico.RECEBIDA
                        && h.getStatusNovo() == StatusOrdemServico.DIAGNOSTICO
                        && "ATUALIZACAO_MANUAL".equals(h.getOrigem())));
    }

    @Test
    void cancelarOrdem_DeveRegistrarHistoricoComMotivo() {
        Long id = 1L;
        OrdemServico os = ordemComStatus(id, StatusOrdemServico.RECEBIDA);
        when(repository.findById(id)).thenReturn(Optional.of(os));
        when(repository.save(any(OrdemServico.class))).thenAnswer(inv -> inv.getArgument(0));

        service.cancelarOrdem(id, "cliente desistiu");

        verify(historicoRepository).save(argThat(h ->
                h.getStatusNovo() == StatusOrdemServico.CANCELADA
                        && "CANCELAMENTO".equals(h.getOrigem())
                        && "cliente desistiu".equals(h.getMotivo())));
    }

    @Test
    void processarEventoSaga_DeveRegistrarHistoricoComOrigemSaga() {
        Long id = 1L;
        OrdemServico os = ordemComStatus(id, StatusOrdemServico.AGUARDANDO_APROVACAO);
        when(repository.findById(id)).thenReturn(Optional.of(os));
        when(repository.save(any(OrdemServico.class))).thenAnswer(inv -> inv.getArgument(0));

        service.processarEventoSaga(eventoSaga(id, StatusOrdemServico.AGUARDANDO_APROVACAO, "orcamento.aprovado"));

        verify(historicoRepository).save(argThat(h ->
                h.getStatusNovo() == StatusOrdemServico.EM_EXECUCAO
                        && "SAGA:orcamento.aprovado".equals(h.getOrigem())));
    }

    @Test
    void processarEventoSaga_NaoDeveRegistrarHistoricoQuandoStatusNaoMuda() {
        Long id = 1L;
        OrdemServico os = ordemComStatus(id, StatusOrdemServico.RECEBIDA);
        when(repository.findById(id)).thenReturn(Optional.of(os));
        when(repository.save(any(OrdemServico.class))).thenAnswer(inv -> inv.getArgument(0));

        service.processarEventoSaga(eventoSaga(id, StatusOrdemServico.RECEBIDA, "evento.desconhecido"));

        verify(historicoRepository, never()).save(any(OrdemServicoHistorico.class));
    }

    private OrdemServico ordemComStatus(Long id, StatusOrdemServico status) {
        OrdemServico os = new OrdemServico();
        os.setId(id);
        os.setStatus(status);
        os.setClienteId("12345678909");
        os.setVeiculoId("ABC1234");
        return os;
    }

    private OSEvent eventoSaga(Long osId, StatusOrdemServico status, String eventType) {
        return new OSEvent(osId, "12345678909", "ABC1234", List.of("1"), List.of("A1"), null, status, eventType);
    }
}
