package com.f1rsters.os_service.service;

import com.f1rsters.os_service.domain.OrdemServico;
import com.f1rsters.os_service.domain.StatusOrdemServico;
import com.f1rsters.os_service.dto.CriarOrdemServicoDTO;
import com.f1rsters.os_service.dto.NotificacaoStatusDTO;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
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
        // Arrange
        CriarOrdemServicoDTO dto = new CriarOrdemServicoDTO();
        dto.cpfCnpjCliente = "12345678909";
        dto.placaVeiculo = "ABC1234";
        dto.servicosIds = List.of("1", "2");
        dto.pecasIds = List.of("1");

        OrdemServico os = new OrdemServico();
        os.setId(1L);
        os.setClienteId("12345678909");
        os.setVeiculoId("ABC1234");
        os.setStatus(StatusOrdemServico.RECEBIDA);

        when(repository.save(any(OrdemServico.class))).thenReturn(os);

        // Act
        OrdemServico result = service.criarOrdem(dto);

        // Assert
        assertNotNull(result);
        assertEquals(StatusOrdemServico.RECEBIDA, result.getStatus());
        verify(repository, times(1)).save(any(OrdemServico.class));
    }

    @Test
    void atualizarStatus_DeveAtualizarStatusComSucesso() {
        // Arrange
        Long id = 1L;
        StatusOrdemServico novoStatus = StatusOrdemServico.EM_EXECUCAO;

        OrdemServico os = new OrdemServico();
        os.setId(id);
        os.setStatus(StatusOrdemServico.RECEBIDA);

        when(repository.findById(id)).thenReturn(Optional.of(os));
        when(repository.save(any(OrdemServico.class))).thenReturn(os);

        // Act
        OrdemServico result = service.atualizarStatus(id, novoStatus);

        // Assert
        assertNotNull(result);
        assertEquals(novoStatus, result.getStatus());
        verify(repository, times(1)).findById(id);
        verify(repository, times(1)).save(any(OrdemServico.class));
    }

    @Test
    void listarTodas_DeveRetornarListaDeOrdens() {
        // Arrange
        OrdemServico os1 = new OrdemServico();
        os1.setId(1L);
        os1.setStatus(StatusOrdemServico.RECEBIDA);

        OrdemServico os2 = new OrdemServico();
        os2.setId(2L);
        os2.setStatus(StatusOrdemServico.EM_EXECUCAO);

        when(repository.findAllActiveOrderByStatusAndDate()).thenReturn(List.of(os1, os2));

        // Act
        List<OrdemServico> result = service.listarTodas();

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
        verify(repository, times(1)).findAllActiveOrderByStatusAndDate();
    }

    @Test
    void detalhar_DeveRetornarOrdemQuandoEncontrada() {
        // Arrange
        Long id = 1L;
        OrdemServico os = new OrdemServico();
        os.setId(id);
        os.setStatus(StatusOrdemServico.RECEBIDA);

        when(repository.findById(id)).thenReturn(Optional.of(os));

        // Act
        OrdemServico result = service.detalhar(id);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(repository, times(1)).findById(id);
    }

    @Test
    void consultarStatus_DeveRetornarStatusDTO() {
        // Arrange
        Long id = 1L;
        OrdemServico os = new OrdemServico();
        os.setId(id);
        os.setStatus(StatusOrdemServico.RECEBIDA);

        when(repository.findById(id)).thenReturn(Optional.of(os));

        // Act
        var result = service.consultarStatus(id);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.osId);
        assertEquals(StatusOrdemServico.RECEBIDA, result.status);
        verify(repository, times(1)).findById(id);
    }

    @Test
    void processarRespostaOrcamento_DeveAtualizarParaEmExecucaoQuandoAprovado() {
        // Arrange
        Long id = 1L;
        RespostaOrcamentoDTO dto = new RespostaOrcamentoDTO();
        dto.aprovado = true;

        OrdemServico os = new OrdemServico();
        os.setId(id);
        os.setStatus(StatusOrdemServico.AGUARDANDO_APROVACAO);

        when(repository.findById(id)).thenReturn(Optional.of(os));
        when(repository.save(any(OrdemServico.class))).thenReturn(os);

        // Act
        OrdemServico result = service.processarRespostaOrcamento(id, dto);

        // Assert
        assertNotNull(result);
        assertEquals(StatusOrdemServico.EM_EXECUCAO, result.getStatus());
        verify(repository, times(1)).findById(id);
        verify(repository, times(1)).save(any(OrdemServico.class));
    }

    @Test
    void processarRespostaOrcamento_DeveManterStatusQuandoReprovado() {
        // Arrange
        Long id = 1L;
        RespostaOrcamentoDTO dto = new RespostaOrcamentoDTO();
        dto.aprovado = false;

        OrdemServico os = new OrdemServico();
        os.setId(id);
        os.setStatus(StatusOrdemServico.AGUARDANDO_APROVACAO);

        when(repository.findById(id)).thenReturn(Optional.of(os));
        when(repository.save(any(OrdemServico.class))).thenReturn(os);

        // Act
        OrdemServico result = service.processarRespostaOrcamento(id, dto);

        // Assert
        assertNotNull(result);
        assertEquals(StatusOrdemServico.AGUARDANDO_APROVACAO, result.getStatus());
        verify(repository, times(1)).findById(id);
        verify(repository, times(1)).save(any(OrdemServico.class));
    }

    @Test
    void processarNotificacaoStatus_DeveAtualizarStatus() {
        // Arrange
        Long id = 1L;
        NotificacaoStatusDTO dto = new NotificacaoStatusDTO();
        dto.novoStatus = StatusOrdemServico.DIAGNOSTICO;

        OrdemServico os = new OrdemServico();
        os.setId(id);
        os.setStatus(StatusOrdemServico.RECEBIDA);

        when(repository.findById(id)).thenReturn(Optional.of(os));
        when(repository.save(any(OrdemServico.class))).thenReturn(os);

        // Act
        OrdemServico result = service.processarNotificacaoStatus(id, dto);

        // Assert
        assertNotNull(result);
        assertEquals(StatusOrdemServico.DIAGNOSTICO, result.getStatus());
        verify(repository, times(1)).findById(id);
        verify(repository, times(1)).save(any(OrdemServico.class));
    }
}
