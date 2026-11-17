# Arquitetura e Saga do OS Service

## Visão geral

Este microsserviço tem responsabilidade central no fluxo de ordens de serviço e atua como ponto de entrada das operações de criação, atualização e consulta da OS.

## Estrutura principal

- API REST: expõe endpoints para criação e acompanhamento da OS
- PostgreSQL: persistência da ordem e do status
- RabbitMQ: comunicação assíncrona com os demais serviços
- Micrometer/Actuator: observabilidade e health checks

## Fluxo de eventos

### 1. Criação da OS

- Cliente envia requisição para `POST /api/ordens-servico`
- Serviço persiste a ordem com status `RECEBIDA`
- Publica `os.created` no `os.exchange` e `os.quote.requested.v1` no `mecanica.events`

### 2. Geração do orçamento

- Billing Service consome `os.quote.requested.v1` (envelope `IntegrationEvent`)
- Cria e submete o orçamento
- Publica `billing.quote.submitted.v1` → OS vai para `AGUARDANDO_APROVACAO`

### 3. Aprovação do orçamento

- Cliente aprova/rejeita via `POST /api/v1/quotes/{id}/approve|reject` no Billing
- Billing publica `billing.quote.approved.v1` ou `billing.quote.rejected.v1`
- OS Service atualiza a ordem conforme o status recebido
- Quando aprovado, a ordem passa para `EM_EXECUCAO` e publica `os.approved`

### 4. Execução do serviço

- Execution Service recebe `os.approved`
- Inicia execução do serviço
- Publica evento de conclusão ou atualização do status

### 5. Conclusão

- OS Service processa a notificação de status final
- Ordem chega em `FINALIZADA`

## Saga Pattern

### Estratégia adotada

Orquestração.

### Motivo

- o fluxo tem sequência lógica bem definida
- o OS Service centraliza a transação principal da ordem
- fica mais simples rastrear falhas, compensações e rollback

## Tratamento de falhas e compensação

O OS Service implementa compensação por **evento compensatório + reversão de estado**, sem transações distribuídas:

| Evento de falha | Ação de compensação no OS Service | Evento publicado |
|---|---|---|
| `billing.quote.rejected.v1` / `billing.quote.failed.v1` (Billing) | OS transiciona para `CANCELADA` (rollback do estado `AGUARDANDO_APROVACAO`) | `os.cancelled` + `os.cancelled.v1` |
| `os.cancelled` (cancelamento externo) | OS transiciona para `CANCELADA` a partir de qualquer status não final | — |
| Falha na publicação de evento | Log de erro + métrica `os.events.published`/`failures`; a OS permanece persistida e o fluxo pode ser retomado por reprocessamento | — |
| Transição de status inválida | Rejeição com `IllegalStateException` (HTTP 400/500) — nenhuma alteração parcial é persistida | — |

Regras de compensação implementadas em `OrdemServicoService.processarEventoBilling`, `processarEventoSaga` e `cancelarOrdem`:

- **Reversão de estado**: a OS volta para `CANCELADA`, desfazendo o avanço no fluxo (ex.: `AGUARDANDO_APROVACAO` → `CANCELADA`).
- **Notificação downstream**: `os.cancelled` é publicado no `os.exchange` (Execution) e `os.cancelled.v1` no `mecanica.events` (Billing) para que os serviços interrompam ações parciais (estorno de orçamento, cancelamento de execução).
- **Idempotência por estado**: transições inválidas são rejeitadas, evitando compensações duplicadas.
- **Rastreabilidade**: cada transição é registrada em log estruturado (MDC) e em métricas Micrometer (`os.status.transitions`, contadores de sucesso/erro por operação).

Exemplos:

- `os.created` falha no Billing Service -> Billing não publica `billing.quote.submitted.v1`; a OS permanece em `RECEBIDA` até rejeição/cancelamento
- `os.approved` não chega ou é rejeitado -> execução não inicia; OS vai para `CANCELADA` com evento `os.cancelled`
- `execution.completed` não chega -> OS permanece em `EM_EXECUCAO`; compensação ocorre via cancelamento (`os.cancelled`) ou intervenção operacional

## Eventos implementados

`os.exchange` (OSEvent):

- `os.created`
- `os.approved`
- `os.status.updated`
- `os.cancelled`
- `execution.completed`

`mecanica.events` (IntegrationEvent):

- `os.quote.requested.v1` (OS -> Billing)
- `os.cancelled.v1` (OS -> Billing)
- `billing.quote.submitted.v1` (Billing -> OS)
- `billing.quote.approved.v1` (Billing -> OS)
- `billing.quote.rejected.v1` (Billing -> OS)
- `billing.quote.failed.v1` (Billing -> OS)

## Observação

Este documento documenta a estratégia correta para o challenge e o desenho que o grupo deve seguir para a atividade do Aluno 1.
