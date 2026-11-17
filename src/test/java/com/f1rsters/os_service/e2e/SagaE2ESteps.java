package com.f1rsters.os_service.e2e;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import io.cucumber.java.Before;
import io.cucumber.java.pt.Dado;
import io.cucumber.java.pt.E;
import io.cucumber.java.pt.Quando;
import io.cucumber.java.pt.Então;
import org.junit.jupiter.api.Assumptions;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.function.BooleanSupplier;

import static org.junit.jupiter.api.Assertions.*;

public class SagaE2ESteps {

    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private final String osUrl = System.getProperty("os.service.url", "http://localhost:8081");
    private final String billingUrl = System.getProperty("billing.service.url", "http://localhost:8080");
    private final String executionUrl = System.getProperty("execution.service.url", "http://localhost:8082");
    private final String rabbitHost = System.getProperty("rabbitmq.host", "localhost");

    private String osId;
    private String quoteId;
    private String executionId;

    private record RestResponse(int status, String body) {
    }

    @Before
    public void ambienteE2EDeveEstarNoAr() {
        Assumptions.assumeTrue(estaNoAr(osUrl), "OS Service não está no ar em " + osUrl);
        Assumptions.assumeTrue(estaNoAr(billingUrl), "Billing Service não está no ar em " + billingUrl);
        Assumptions.assumeTrue(estaNoAr(executionUrl), "Execution Service não está no ar em " + executionUrl);
    }

    @Dado("que o ambiente E2E está no ar")
    public void ambienteNoAr() {
        // validado no @Before
    }

    @Quando("eu crio uma OS com CPF {string} e placa {string}")
    public void criarOs(String cpf, String placa) throws Exception {
        String body = "{\"cpfCnpjCliente\":\"" + cpf + "\",\"placaVeiculo\":\"" + placa
                + "\",\"servicosIds\":[\"1\",\"2\"],\"pecasIds\":[\"10\"]}";
        RestResponse r = request("POST", osUrl + "/api/ordens-servico", body);
        assertTrue(r.status() == 200 || r.status() == 201,
                "POST OS falhou HTTP " + r.status() + ": " + r.body());
        osId = MAPPER.readTree(r.body()).get("id").asText();
        assertNotNull(osId);
    }

    @Então("a OS deve estar com status {string}")
    public void osDeveEstarComStatus(String esperado) {
        aguardar("OS " + osId + " -> " + esperado, () -> {
            RestResponse r = get(osUrl + "/api/ordens-servico/" + osId + "/status");
            if (r.status() != 200) {
                return false;
            }
            try {
                return esperado.equals(MAPPER.readTree(r.body()).get("status").asText());
            } catch (Exception e) {
                return false;
            }
        }, 90);
    }

    @E("deve existir um orçamento no Billing para a OS")
    public void buscarOrcamento() {
        aguardar("orçamento da OS " + osId + " no Billing", () -> {
            RestResponse r = get(billingUrl + "/api/v1/quotes?osId=" + osId);
            if (r.status() != 200) {
                return false;
            }
            try {
                JsonNode id = MAPPER.readTree(r.body()).get("id");
                if (id != null && !id.isNull()) {
                    quoteId = id.asText();
                    return true;
                }
            } catch (Exception ignored) {
            }
            return false;
        }, 60);
        assertNotNull(quoteId, "Orçamento não encontrado no Billing para osId=" + osId);
    }

    @Quando("eu aprovo o orçamento no Billing")
    public void aprovarOrcamento() {
        RestResponse r = request("POST", billingUrl + "/api/v1/quotes/" + quoteId + "/approve", null);
        assertEquals(200, r.status(), "Approve do orçamento falhou: " + r.body());
    }

    @Quando("eu rejeito o orçamento no Billing com motivo {string}")
    public void rejeitarOrcamento(String motivo) {
        RestResponse r = request("POST", billingUrl + "/api/v1/quotes/" + quoteId + "/reject",
                "{\"reason\":\"" + motivo + "\"}");
        assertEquals(200, r.status(), "Reject do orçamento falhou: " + r.body());
    }

    @E("deve existir uma execução no Execution para a OS")
    public void buscarExecucao() {
        aguardar("execução da OS " + osId + " no Execution", () -> {
            RestResponse r = get(executionUrl + "/api/executions/queue");
            if (r.status() != 200) {
                return false;
            }
            try {
                for (JsonNode e : MAPPER.readTree(r.body())) {
                    JsonNode osNode = e.get("osId");
                    if (osNode != null && osNode.asText().equals(osId)) {
                        executionId = e.get("executionId").asText();
                        return true;
                    }
                }
            } catch (Exception ignored) {
            }
            return false;
        }, 60);
        assertNotNull(executionId, "Execução não encontrada no Execution para osId=" + osId);
    }

    @Quando("eu completo a execução no Execution")
    public void completarExecucao() {
        RestResponse r = request("POST", executionUrl + "/api/executions/" + executionId + "/complete", null);
        assertEquals(200, r.status(), "Complete da execução falhou: " + r.body());
    }

    @Quando("eu publico o evento de cancelamento da OS no RabbitMQ")
    public void publicarCancelamento() throws Exception {
        String json = "{\"eventId\":\"" + UUID.randomUUID() + "\",\"osId\":" + osId
                + ",\"eventType\":\"os.cancelled\",\"status\":\"CANCELADA\""
                + ",\"timestamp\":\"" + OffsetDateTime.now() + "\"}";
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(rabbitHost);
        try (Connection connection = factory.newConnection();
             Channel channel = connection.createChannel()) {
            channel.basicPublish("os.exchange", "os.cancelled",
                    new AMQP.BasicProperties.Builder().contentType("application/json").build(),
                    json.getBytes(StandardCharsets.UTF_8));
        }
    }

    @E("a execução deve estar com status {string}")
    public void execucaoDeveEstarComStatus(String esperado) {
        aguardar("execução " + executionId + " -> " + esperado, () -> {
            RestResponse r = get(executionUrl + "/api/executions/" + executionId + "/history");
            if (r.status() != 200) {
                return false;
            }
            try {
                for (JsonNode e : MAPPER.readTree(r.body())) {
                    JsonNode toStatus = e.get("toStatus");
                    if (toStatus != null && esperado.equals(toStatus.asText())) {
                        return true;
                    }
                }
            } catch (Exception ignored) {
            }
            return false;
        }, 60);
    }

    private RestResponse get(String url) {
        return request("GET", url, null);
    }

    private RestResponse request(String method, String url, String jsonBody) {
        try {
            HttpRequest.Builder builder = HttpRequest.newBuilder(URI.create(url))
                    .timeout(Duration.ofSeconds(15))
                    .header("Content-Type", "application/json");
            if (jsonBody != null) {
                builder.method(method, HttpRequest.BodyPublishers.ofString(jsonBody));
            } else if ("GET".equals(method)) {
                builder.GET();
            } else {
                builder.method(method, HttpRequest.BodyPublishers.noBody());
            }
            HttpResponse<String> response = HTTP.send(builder.build(), HttpResponse.BodyHandlers.ofString());
            return new RestResponse(response.statusCode(), response.body());
        } catch (Exception e) {
            throw new RuntimeException("Falha na requisição " + method + " " + url, e);
        }
    }

    private boolean estaNoAr(String baseUrl) {
        try {
            RestResponse r = get(baseUrl + "/actuator/health");
            return r.status() == 200;
        } catch (Exception e) {
            return false;
        }
    }

    private void aguardar(String descricao, BooleanSupplier condicao, int timeoutSegundos) {
        long deadline = System.currentTimeMillis() + timeoutSegundos * 1000L;
        while (System.currentTimeMillis() < deadline) {
            if (condicao.getAsBoolean()) {
                return;
            }
            dormir(2000);
        }
        fail("Tempo esgotado aguardando: " + descricao);
    }

    private void dormir(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
