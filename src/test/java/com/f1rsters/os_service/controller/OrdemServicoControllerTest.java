package com.f1rsters.os_service.controller;

import com.f1rsters.os_service.domain.OrdemServico;
import com.f1rsters.os_service.domain.StatusOrdemServico;
import com.f1rsters.os_service.dto.AtualizarStatusOSDTO;
import com.f1rsters.os_service.dto.CriarOrdemServicoDTO;
import com.f1rsters.os_service.dto.NotificacaoStatusDTO;
import com.f1rsters.os_service.dto.RespostaOrcamentoDTO;
import com.f1rsters.os_service.dto.StatusOrdemServicoDTO;
import com.f1rsters.os_service.service.OrdemServicoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrdemServicoController.class)
class OrdemServicoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @MockBean
    private OrdemServicoService service;

    @Test
    void criar_DeveRetornarOrdemCriada() throws Exception {
        CriarOrdemServicoDTO dto = new CriarOrdemServicoDTO();
        dto.setCpfCnpjCliente("10000000000");
        dto.setPlacaVeiculo("ABC1234");
        dto.setServicosIds(List.of("1", "2"));
        dto.setPecasIds(List.of("10"));

        OrdemServico os = new OrdemServico();
        os.setId(1L);
        os.setStatus(StatusOrdemServico.RECEBIDA);

        when(service.criarOrdem(any(CriarOrdemServicoDTO.class))).thenReturn(os);

        mockMvc.perform(post("/api/ordens-servico")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.status").value("RECEBIDA"));
    }

    @Test
    void atualizarStatus_DeveRetornarOrdemAtualizada() throws Exception {
        Long id = 1L;
        AtualizarStatusOSDTO dto = new AtualizarStatusOSDTO();
        dto.novoStatus = StatusOrdemServico.EM_EXECUCAO;

        OrdemServico os = new OrdemServico();
        os.setId(id);
        os.setStatus(StatusOrdemServico.EM_EXECUCAO);

        when(service.atualizarStatus(eq(id), eq(StatusOrdemServico.EM_EXECUCAO))).thenReturn(os);

        mockMvc.perform(patch("/api/ordens-servico/{id}/status", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.status").value("EM_EXECUCAO"));
    }

    @Test
    void listar_DeveRetornarListaDeOrdens() throws Exception {
        OrdemServico os1 = new OrdemServico();
        os1.setId(1L);
        os1.setStatus(StatusOrdemServico.RECEBIDA);

        OrdemServico os2 = new OrdemServico();
        os2.setId(2L);
        os2.setStatus(StatusOrdemServico.EM_EXECUCAO);

        when(service.listarTodas()).thenReturn(List.of(os1, os2));

        mockMvc.perform(get("/api/ordens-servico"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$.length()").value(2));
    }

    @Test
    void detalhar_DeveRetornarOrdem() throws Exception {
        Long id = 1L;
        OrdemServico os = new OrdemServico();
        os.setId(id);
        os.setStatus(StatusOrdemServico.RECEBIDA);

        when(service.detalhar(id)).thenReturn(os);

        mockMvc.perform(get("/api/ordens-servico/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.status").value("RECEBIDA"));
    }

    @Test
    void consultarStatus_DeveRetornarStatusDTO() throws Exception {
        Long id = 1L;
        StatusOrdemServicoDTO dto = StatusOrdemServicoDTO.from(id, StatusOrdemServico.RECEBIDA, LocalDateTime.now());

        when(service.consultarStatus(id)).thenReturn(dto);

        mockMvc.perform(get("/api/ordens-servico/{id}/status", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.osId").value(id))
                .andExpect(jsonPath("$.status").value("RECEBIDA"));
    }

    @Test
    void responderOrcamento_DeveRetornarOrdemAtualizada() throws Exception {
        Long id = 1L;
        RespostaOrcamentoDTO dto = new RespostaOrcamentoDTO();
        dto.aprovado = true;

        OrdemServico os = new OrdemServico();
        os.setId(id);
        os.setStatus(StatusOrdemServico.EM_EXECUCAO);

        when(service.processarRespostaOrcamento(eq(id), any(RespostaOrcamentoDTO.class))).thenReturn(os);

        mockMvc.perform(post("/api/ordens-servico/{id}/orcamento/resposta", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.status").value("EM_EXECUCAO"));
    }

    @Test
    void notificarStatus_DeveRetornarOrdemAtualizada() throws Exception {
        Long id = 1L;
        NotificacaoStatusDTO dto = new NotificacaoStatusDTO();
        dto.novoStatus = StatusOrdemServico.DIAGNOSTICO;

        OrdemServico os = new OrdemServico();
        os.setId(id);
        os.setStatus(StatusOrdemServico.DIAGNOSTICO);

        when(service.processarNotificacaoStatus(eq(id), any(NotificacaoStatusDTO.class))).thenReturn(os);

        mockMvc.perform(post("/api/ordens-servico/{id}/status/notificacao", id)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.status").value("DIAGNOSTICO"));
    }
}
