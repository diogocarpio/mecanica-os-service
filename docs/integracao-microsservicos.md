# Integração entre Microsserviços

Este documento descreve como o OS Service se integra com o Billing Service e o Execution Service no ecossistema da oficina mecânica.

## Visão geral

A integração é **assíncrona, via RabbitMQ**, seguindo o padrão **Saga coreografada**: não existe orquestrador central nem chamadas REST entre microsserviços. Cada serviço executa sua transação local de forma independente e reage aos eventos publicados pelos demais.

- **Nenhum serviço acessa o banco de outro serviço.**
- Toda a coordenação do fluxo acontece por eventos versionados e pela máquina de estados do OS Service.

## Exchanges e contratos

| Exchange | Contrato | Uso |
|---|---|---|
| `os.exchange` | `OSEvent` (payload plano) | Ciclo de vida da OS entre OS Service e Execution Service |
| `mecanica.events` | `IntegrationEvent<T>` (envelope versionado) | Orçamento entre OS Service e Billing Service |

Detalhes de payloads, filas e regras de transição: `docs/contrato-eventos-saga.md`.

## Mapa de integração

### OS Service → Billing Service (`mecanica.events`)

| Evento | Quando | Efeito esperado no Billing |
|---|---|---|
| `os.quote.requested.v1` | Criação da OS | Cria e submete o orçamento |
| `os.cancelled.v1` | Cancelamento da OS | Compensa o orçamento (cancela se ainda pendente) |

### Billing Service → OS Service (`mecanica.events`)

| Evento | Quando | Efeito no OS Service |
|---|---|---|
| `billing.quote.submitted.v1` | Orçamento submetido | `RECEBIDA`/`DIAGNOSTICO` → `AGUARDANDO_APROVACAO` |
| `billing.quote.approved.v1` | Orçamento aprovado | → `AGUARDANDO_PAGAMENTO` (aguarda pagamento) |
| `billing.quote.rejected.v1` | Orçamento rejeitado | → `CANCELADA` + compensações |
| `billing.quote.failed.v1` | Falha na geração do orçamento | → `CANCELADA` + compensações |
| `billing.payment.pending.v1` | Pagamento em processamento | Sem mudança de status (permanece `AGUARDANDO_PAGAMENTO`) |
| `billing.payment.approved.v1` | Pagamento confirmado | → `EM_EXECUCAO` + publica `os.approved` |
| `billing.payment.rejected.v1` | Pagamento rejeitado | → `CANCELADA` + compensações |
| `billing.payment.failed.v1` | Falha no pagamento | → `CANCELADA` + compensações |

### OS Service → Execution Service (`os.exchange`)

| Evento | Quando | Efeito esperado no Execution |
|---|---|---|
| `os.created` | Criação da OS | Sem consumidor no Execution — mantido para observabilidade/compatibilidade |
| `os.approved` | Pagamento aprovado | Inicia a execução do serviço |
| `os.cancelled` | Cancelamento da OS | Cancela a execução |
| `os.status.updated` | OS finalizada | Notificação do ciclo de vida (monitoramento/outros consumidores) |

### Execution Service → OS Service (`os.exchange`)

| Evento | Quando | Efeito no OS Service |
|---|---|---|
| `execution.completed` | Serviço executado com sucesso | → `FINALIZADA` + publica `os.status.updated` |

## Pontos de contato REST

A integração entre serviços **não usa REST**. As APIs REST de cada serviço são consumidas por clientes externos:

- **Billing Service**: `POST /api/v1/quotes/{id}/approve` e `POST /api/v1/quotes/{id}/reject` são chamados pelo cliente para aprovar/rejeitar o orçamento — o resultado chega ao OS Service via eventos.
- **OS Service**: `POST /api/ordens-servico` (criação), `PATCH /api/ordens-servico/{id}/status`, consultas de status/histórico.
- **Execution Service**: `POST /api/executions/authorization`, `/{id}/diagnosis/start`, `/{id}/repairs/start`, `/{id}/complete`, `/{id}/cancel` — usados operacionalmente; o fluxo Saga usa os eventos.

## Compensação (rollback)

Em caso de falha em qualquer etapa, a compensação é feita por **eventos compensatórios + reversão de estado local**:

- Orçamento rejeitado/falho → OS vai para `CANCELADA` e publica `os.cancelled` (Execution) + `os.cancelled.v1` (Billing).
- Pagamento rejeitado/falho → OS vai para `CANCELADA` e publica `os.cancelled` + `os.cancelled.v1` (a execução nunca é iniciada).
- Cancelamento da OS → `os.cancelled` + `os.cancelled.v1` notificam os participantes para encerrarem ações pendentes.
- Cada serviço reverte apenas o **seu** estado local — nunca há transação distribuída nem escrita cruzada em banco.

## Confiabilidade

- **Idempotência**: consumidores ignoram eventos duplicados ou fora de ordem (transições de status inválidas são rejeitadas).
- **Rastreabilidade**: `eventId` (UUID por evento) e `correlationId` (= `osId`) correlacionam todo o fluxo entre serviços.
- **Resiliência**: filas duráveis + reprocessamento; se um serviço estiver fora do ar, os eventos aguardam na fila.

## Gaps conhecidos

- O pagamento real depende do Mercado Pago (webhook + reconciliação no Billing). No E2E (`e2e/run-e2e.sh` e `features-e2e`), o evento `billing.payment.approved.v1` é **publicado manualmente** no RabbitMQ para simular a confirmação do provedor — não há stub do Mercado Pago no ambiente de teste.
