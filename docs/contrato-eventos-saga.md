# Contrato de Eventos e Saga do OS Service

## Visão geral

Este documento define o contrato dos eventos usados no fluxo de ordens de serviço entre os microsserviços do grupo. O objetivo é manter a integração desacoplada, rastreável e compatível com o padrão Saga.

## Estratégia adotada

Padrão Saga por orquestração.

O OS Service atua como ponto central da ordem de serviço e coordena o fluxo principal:

1. Criação da OS
2. Solicitação de orçamento ao Billing
3. Aprovação do orçamento
4. Execução do serviço
5. Compensação em caso de falha

## Canais de mensageria

O ecossistema usa **dois exchanges RabbitMQ**:

| Exchange | Uso | Contrato |
|---|---|---|
| `os.exchange` | Eventos do ciclo de vida da OS (Execution Service e consumidores internos) | `OSEvent` (payload plano) |
| `mecanica.events` | Eventos de orçamento entre OS Service e Billing Service | `IntegrationEvent<T>` (envelope versionado) |

### OSEvent (os.exchange)

Payload plano consumido pelo Execution Service:

```json
{
  "eventId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "osId": 1,
  "clienteId": "10000000000",
  "veiculoId": "ABC1234",
  "servicosIds": ["1", "2"],
  "pecasIds": ["10"],
  "valorTotal": 0.00,
  "status": "RECEBIDA",
  "eventType": "os.created",
  "timestamp": "2026-11-16T20:00:00-03:00"
}
```

- `eventId`: UUID gerado pelo produtor (o Execution resolve via `EventIdUtil` quando ausente)
- `timestamp`: `OffsetDateTime` ISO-8601 com offset
- `status`: nome do `StatusOrdemServico`

### IntegrationEvent (mecanica.events)

Envelope versionado exigido pelo Billing Service:

```json
{
  "eventId": "3fa85f64-5717-4562-b3fc-2c963f66afa6",
  "eventType": "os.quote.requested.v1",
  "eventVersion": 1,
  "occurredAt": "2026-11-16T20:00:00Z",
  "source": "os-service",
  "correlationId": "1",
  "causationId": null,
  "osId": "1",
  "payload": { }
}
```

- `source`: sempre `"os-service"` nos eventos produzidos pelo OS Service (o Billing valida esse valor)
- `correlationId`: obrigatório (usamos o `osId` como correlação)
- `osId`: `String` com o id da OS
- `eventVersion`: `1`

---

## Status da ordem de serviço

A ordem deve seguir este ciclo principal:

```text
RECEBIDA -> AGUARDANDO_APROVACAO -> EM_EXECUCAO -> FINALIZADA
```

Em caso de falha ou compensação:

```text
RECEBIDA / DIAGNOSTICO / AGUARDANDO_APROVACAO / EM_EXECUCAO -> CANCELADA
```

Os estados previstos pelo serviço são:

- RECEBIDA
- DIAGNOSTICO
- AGUARDANDO_APROVACAO
- EM_EXECUCAO
- FINALIZADA
- ENTREGUE
- CANCELADA

---

## Eventos do fluxo

### 1) os.quote.requested.v1 (OS -> Billing)

- Exchange: `mecanica.events`
- Publicado por: OS Service
- Consumido por: Billing Service (fila `billing.os.quote-requested.v1`)
- Quando: após a criação da OS
- Descrição: solicita a geração do orçamento

Payload:

```json
{
  "customerId": "10000000000",
  "items": [
    { "description": "Servico 1", "type": "LABOR", "quantity": 1, "unitPrice": 0 },
    { "description": "Peca 10", "type": "PART", "quantity": 1, "unitPrice": 0 }
  ],
  "currency": "BRL",
  "validUntil": "2026-11-23T20:00:00Z"
}
```

Regras do contrato do Billing:

- `items[].type`: `PART`, `LABOR` ou `OTHER`
- `items[].quantity`: maior que zero
- `items[].unitPrice`: maior ou igual a zero (o OS envia `0` — o preço oficial é definido pelo Billing)
- `currency`: ISO 4217 alpha-3 (`BRL`)
- `validUntil`: no futuro

Ação esperada:
- Billing cria e submete o orçamento
- publica `billing.quote.submitted.v1`, `billing.quote.approved.v1` ou `billing.quote.rejected.v1`

> O OS Service também publica `os.created` no `os.exchange` (OSEvent) na criação, mantido para compatibilidade e observabilidade.

---

### 2) billing.quote.submitted.v1 (Billing -> OS)

- Exchange: `mecanica.events`
- Publicado por: Billing Service
- Consumido por: OS Service (fila `os.billing.quote-submitted.queue`)
- Quando: orçamento criado e submetido para aprovação
- Descrição: move a OS de `RECEBIDA`/`DIAGNOSTICO` para `AGUARDANDO_APROVACAO`

Payload (`QuoteSubmittedPayload`):

```json
{
  "quoteId": "uuid-do-orcamento",
  "total": 0.00,
  "currency": "BRL",
  "status": "PENDING",
  "validUntil": "2026-11-23T20:00:00Z"
}
```

---

### 3) billing.quote.approved.v1 (Billing -> OS)

- Exchange: `mecanica.events`
- Publicado por: Billing Service
- Consumido por: OS Service (fila `os.billing.quote-approved.queue`)
- Quando: orçamento aprovado (via `POST /api/v1/quotes/{id}/approve` no Billing)
- Descrição: indica que a OS pode seguir para execução

Payload (`QuoteApprovedPayload`):

```json
{
  "quoteId": "uuid-do-orcamento",
  "total": 0.00,
  "currency": "BRL",
  "status": "APPROVED",
  "decidedAt": "2026-11-16T20:05:00Z"
}
```

Ação esperada no OS Service:
- atualiza o status da OS para `EM_EXECUCAO` (com auto-avanço de `RECEBIDA`/`DIAGNOSTICO` para `AGUARDANDO_APROVACAO` se necessário)
- é idempotente: ignora se a OS já está `EM_EXECUCAO`, `FINALIZADA` ou `ENTREGUE`
- publica `os.approved` no `os.exchange`

---

### 4) billing.quote.rejected.v1 (Billing -> OS)

- Exchange: `mecanica.events`
- Publicado por: Billing Service
- Consumido por: OS Service (fila `os.billing.quote-rejected.queue`)
- Quando: orçamento rejeitado
- Descrição: indica falha no fluxo de aprovação

Payload (`QuoteRejectedPayload`):

```json
{
  "quoteId": "uuid-do-orcamento",
  "status": "REJECTED",
  "reason": "motivo da rejeição",
  "decidedAt": "2026-11-16T20:06:00Z"
}
```

Ação esperada:
- OS Service marca a ordem como `CANCELADA`
- publica `os.cancelled` no `os.exchange` e `os.cancelled.v1` no `mecanica.events`
- é idempotente: ignora se a OS já está `CANCELADA`

---

### 5) billing.quote.failed.v1 (Billing -> OS)

- Exchange: `mecanica.events`
- Publicado por: Billing Service
- Consumido por: OS Service (fila `os.billing.quote-failed.queue`)
- Quando: falha na geração do orçamento (dados inválidos, conflito, etc.)
- Descrição: compensação do pedido de orçamento

Payload (`QuoteFailedPayload`):

```json
{
  "code": "INVALID_QUOTE_DATA",
  "message": "detalhe do erro",
  "failedAt": "2026-11-16T20:07:00Z"
}
```

Ação esperada: mesma de `billing.quote.rejected.v1` (OS -> `CANCELADA` + compensações).

---

### 6) os.approved (OS -> Execution)

- Exchange: `os.exchange`
- Publicado por: OS Service
- Consumido por: Execution Service (fila `execution.os.approved.queue`)
- Quando: a aprovação do orçamento foi processada
- Descrição: sinaliza que a execução pode iniciar

Payload: `OSEvent` com `status: "EM_EXECUCAO"` e `eventType: "os.approved"`.

Ação esperada:
- Execution Service inicia o serviço

---

### 7) execution.completed (Execution -> OS)

- Exchange: `os.exchange`
- Publicado por: Execution Service
- Consumido por: OS Service (fila `execution.completed.queue`)
- Quando: o serviço foi executado com sucesso
- Descrição: conclui a ordem de serviço

Payload: `OSEvent` com `status: "FINALIZADA"` e `eventType: "execution.completed"`.

Ação esperada no OS Service:
- atualiza a OS para `FINALIZADA`
- publica `os.status.updated`

---

### 8) os.cancelled (OS -> Execution / consumidores)

- Exchange: `os.exchange`
- Publicado por: OS Service
- Consumido por: Execution Service (fila `execution.os.cancelled.queue`) e pela própria fila `os.cancelled.queue` do OS
- Quando: cancelamento da OS
- Descrição: cancela a operação distribuída

Payload: `OSEvent` com `status: "CANCELADA"` e `eventType: "os.cancelled"`.

---

### 9) os.cancelled.v1 (OS -> Billing)

- Exchange: `mecanica.events`
- Publicado por: OS Service
- Consumido por: Billing Service (fila `billing.os.cancelled.v1`)
- Quando: cancelamento da OS
- Descrição: compensação do orçamento no Billing

Payload (`OsCancelledPayload`):

```json
{
  "reason": "motivo do cancelamento",
  "cancelledAt": "2026-11-16T20:08:00Z"
}
```

---

### 10) os.status.updated (OS -> monitoramento)

- Exchange: `os.exchange`
- Publicado por: OS Service
- Consumido por: monitoramento / outros serviços de integração
- Quando: a OS é finalizada (após `execution.completed`)
- Descrição: notificação do ciclo de vida da ordem

Payload: `OSEvent` com o status atualizado.

---

## Regras de transição de status

| Status atual | Próximo status válido |
|---|---|
| RECEBIDA | DIAGNOSTICO, AGUARDANDO_APROVACAO, CANCELADA |
| DIAGNOSTICO | AGUARDANDO_APROVACAO, CANCELADA |
| AGUARDANDO_APROVACAO | EM_EXECUCAO, CANCELADA |
| EM_EXECUCAO | FINALIZADA, CANCELADA |
| FINALIZADA | ENTREGUE, CANCELADA |
| ENTREGUE | nenhuma |
| CANCELADA | nenhuma |

Qualquer outra transição deve ser rejeitada como inválida.

O tratamento de eventos do Billing (`processarEventoBilling`) é idempotente e tolerante a reordenação: `billing.quote.approved.v1` auto-avança `RECEBIDA`/`DIAGNOSTICO` para `AGUARDANDO_APROVACAO` antes de ir para `EM_EXECUCAO`.

---

## Compensação e rollback

Quando ocorre falha em alguma etapa do fluxo, o sistema deve:

1. identificar a falha
2. registrar motivo logado e com métrica
3. atualizar a OS para `CANCELADA`
4. publicar `os.cancelled` (os.exchange) e `os.cancelled.v1` (mecanica.events)
5. permitir que o restante do ecossistema encerre ou reverta ações pendentes

Fluxos de compensação esperados:

- falha no orçamento: `billing.quote.rejected.v1` / `billing.quote.failed.v1` -> `CANCELADA` + `os.cancelled.v1`
- cancelamento manual: `cancelarOrdem` -> `CANCELADA` + `os.cancelled` + `os.cancelled.v1`
- falha na execução: evento equivalente -> `os.cancelled`

---

## Observações para o grupo

- O contrato acima reflete o que está implementado nos três repositórios (`mecanica-os-service`, `mecanica-billing-service`, `mecanica-execution-service`).
- O `eventType`/routing key é obrigatório para rastreio e observabilidade.
- O `timestamp`/`occurredAt` deve ser preenchido em todos os eventos.
- O `osId`/`correlationId` deve ser o identificador da ordem de serviço para correlação entre serviços.
- O Billing valida `source == "os-service"` e `correlationId` não vazio nos eventos recebidos.

---

## Resumo executivo

O contrato aqui definido garante que:

- a ordem de serviço é rastreada em todo o fluxo
- todos os serviços sabem qual evento foi publicado
- a transação distribuída pode ser compensada em caso de falha
- o OS Service mantém o controle do ciclo de vida da ordem

Este contrato é o ponto de alinhamento entre o OS Service, o Billing Service e o Execution Service.
