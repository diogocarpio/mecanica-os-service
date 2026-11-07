package com.f1rsters.os_service.bdd;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.f1rsters.os_service.dto.CriarOrdemServicoDTO;
import com.f1rsters.os_service.dto.NotificacaoStatusDTO;
import com.f1rsters.os_service.dto.OSEvent;
import com.f1rsters.os_service.dto.RespostaOrcamentoDTO;
import com.f1rsters.os_service.listener.OrdemServicoSagaListener;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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

    @Autowired
    private OrdemServicoSagaListener sagaListener;

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

    @And("o billing service publica o evento de orçamento aprovado")
    public void billingPublicaOrcamentoAprovado() {
        sagaListener.onOrcamentoAprovado(eventoSaga("orcamento.aprovado"));
        verify(rabbitTemplate).convertAndSend(eq("os.exchange"), eq("os.approved"), any(OSEvent.class));
    }

    @And("o billing service publica o evento de orçamento rejeitado")
    public void billingPublicaOrcamentoRejeitado() {
        sagaListener.onOrcamentoRejeitado(eventoSaga("orcamento.rejeitado"));
        verify(rabbitTemplate).convertAndSend(eq("os.exchange"), eq("os.cancelled"), any(OSEvent.class));
    }

    @When("o execution service publica o evento de execução concluída")
    public void executionPublicaExecucaoConcluida() {
        sagaListener.onExecutionCompleted(eventoSaga("execution.completed"));
        verify(rabbitTemplate).convertAndSend(eq("os.exchange"), eq("os.status.updated"), any(OSEvent.class));
    }

    @When("um evento de cancelamento da OS é recebido")
    public void eventoCancelamentoRecebido() {
        sagaListener.onOsCancelled(eventoSaga("os.cancelled"));
    }

    private OSEvent eventoSaga(String eventType) {
        return new OSEvent(osId, "cliente-teste", "veiculo-teste",
                java.util.List.of("1"), java.util.List.of("10"), null, null, eventType);
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
