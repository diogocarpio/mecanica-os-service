package com.f1rsters.os_service.bdd;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.f1rsters.os_service.dto.CriarOrdemServicoDTO;
import com.f1rsters.os_service.dto.NotificacaoStatusDTO;
import com.f1rsters.os_service.dto.RespostaOrcamentoDTO;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@CucumberContextConfiguration
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class OSServiceSagaSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RabbitTemplate rabbitTemplate;

    private Long osId;

    @Before
    public void resetState() {
        osId = null;
    }

    @Given("uma OS é criada com CPF {string} e placa {string}")
    public void criarOS(String cpf, String placa) throws Exception {
        CriarOrdemServicoDTO dto = new CriarOrdemServicoDTO();
        dto.setCpfCnpjCliente(cpf);
        dto.setPlacaVeiculo(placa);
        dto.setServicosIds(java.util.List.of("1", "2"));
        dto.setPecasIds(java.util.List.of("10"));

        MvcResult result = mockMvc.perform(post("/api/ordens-servico")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        osId = body.get("id").asLong();
        verify(rabbitTemplate).convertAndSend(eq("os.exchange"), eq("os.created"), any(Object.class));
    }

    @When("o billing informa que o orçamento foi gerado e a OS entrou em aguardando aprovação")
    public void atualizarParaAguardandoAprovacao() throws Exception {
        NotificacaoStatusDTO dto = new NotificacaoStatusDTO();
        dto.novoStatus = com.f1rsters.os_service.domain.StatusOrdemServico.AGUARDANDO_APROVACAO;

        mockMvc.perform(post("/api/ordens-servico/{id}/status/notificacao", osId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @When("o serviço de billing aprova o orçamento")
    public void aprovarOrcamento() throws Exception {
        RespostaOrcamentoDTO dto = new RespostaOrcamentoDTO();
        dto.aprovado = true;

        mockMvc.perform(post("/api/ordens-servico/{id}/orcamento/resposta", osId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @When("o serviço de execução finaliza a ordem")
    public void finalizarOrdem() throws Exception {
        NotificacaoStatusDTO dto = new NotificacaoStatusDTO();
        dto.novoStatus = com.f1rsters.os_service.domain.StatusOrdemServico.FINALIZADA;

        mockMvc.perform(post("/api/ordens-servico/{id}/status/notificacao", osId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
    }

    @Then("o status da OS deve ser {string}")
    public void verificarStatus(String statusEsperado) throws Exception {
        MvcResult result = mockMvc.perform(get("/api/ordens-servico/{id}/status", osId))
                .andExpect(status().isOk())
                .andReturn();

        JsonNode body = objectMapper.readTree(result.getResponse().getContentAsString());
        assertEquals(statusEsperado, body.get("status").asText());
    }

    @And("o status da OS deve ser {string} imediatamente após a criação")
    public void verificarStatusImediatamente(String statusEsperado) throws Exception {
        verificarStatus(statusEsperado);
    }
}
