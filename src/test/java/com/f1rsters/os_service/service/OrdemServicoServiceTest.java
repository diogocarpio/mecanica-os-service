package com.f1rsters.os_service.service;

import com.f1rsters.os_service.domain.OrdemServico;
import com.f1rsters.os_service.domain.StatusOrdemServico;
import com.f1rsters.os_service.dto.CriarOrdemServicoDTO;
import com.f1rsters.os_service.dto.NotificacaoStatusDTO;
import com.f1rsters.os_service.dto.OSEvent;
import com.f1rsters.os_service.dto.RespostaOrcamentoDTO;
import com.f1rsters.os_service.repository.OrdemServicoRepository;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
    private RabbitTemplate rabbitTemplate;

    private MeterRegistry meterRegistry;
    private OrdemServicoService service;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        service = new OrdemServicoService(repository, meterRegistry, rabbitTemplate);
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
        StatusOrdemServico novoStatus = StatusOrdemServico.EM_EXECUCAO;

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
}
