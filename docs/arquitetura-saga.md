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
- Quando aprovado, a ordem passa para `AGUARDANDO_PAGAMENTO` (o Billing cria o link de pagamento após a aprovação do orçamento)

### 4. Pagamento do orçamento

- Cliente paga via link do Mercado Pago; o Billing confirma pelo webhook/reconciliação
- Billing publica `billing.payment.pending.v1`, `billing.payment.approved.v1`, `billing.payment.rejected.v1` ou `billing.payment.failed.v1`
- `billing.payment.approved.v1` → OS vai para `EM_EXECUCAO` e publica `os.approved`
- `billing.payment.rejected.v1` / `billing.payment.failed.v1` → OS vai para `CANCELADA` com compensações (`os.cancelled` + `os.cancelled.v1`)

### 5. Execução do serviço

- Execution Service recebe `os.approved`
- Inicia execução do serviço
- Publica evento de conclusão ou atualização do status

### 6. Conclusão

- OS Service processa a notificação de status final
- Ordem chega em `FINALIZADA`

## Saga Pattern

### Estratégia adotada

**Coreografia**, via eventos do RabbitMQ. Não existe um orquestrador central: cada microsserviço executa sua transação local de forma independente e reage aos eventos publicados pelos demais.

### Como a coreografia funciona na prática

- O OS Service **não comanda** os outros serviços — ele publica **fatos** (`os.created`, `os.quote.requested.v1`, `os.approved`, `os.cancelled`, `os.status.updated`) e **reage** a eventos (`billing.quote.*`, `billing.payment.*`, `execution.completed`, `os.cancelled`).
- O Billing Service decide localmente criar/aprovar/rejeitar o orçamento e publica o resultado (`billing.quote.*`).
- O Execution Service decide localmente iniciar/concluir/cancelar a execução e publica o resultado (`execution.completed`, atualizações de status).
- A "coordenação" emerge da máquina de estados do OS Service (`StatusOrdemServico` + `validarTransicaoStatus`), que é a fonte da verdade do ciclo de vida da OS.

### Por que coreografia (e não orquestração)

- **Baixo acoplamento**: nenhum serviço precisa conhecer endpoints ou comandos dos outros — apenas os contratos de eventos.
- **Resiliência**: se um serviço estiver fora do ar, os eventos ficam na fila e o fluxo retoma quando ele voltar, sem um orquestrador precisando reenviar comandos.
- **Escalabilidade**: cada serviço escala e falha de forma independente.
- **Simplicidade operacional**: não há um ponto único de falha (orquestrador) nem necessidade de persistir estado de saga em um banco central.
- O fluxo é naturalmente orientado a eventos (aprovação humana do orçamento, conclusão física do reparo), o que favorece a coreografia.

### Trade-off aceito

- A coreografia distribui a lógica do fluxo entre os serviços, então a **documentação dos contratos de eventos** (`docs/contrato-eventos-saga.md`) e a **máquina de estados do OS** são essenciais para rastreabilidade — ambos estão documentados e cobertos por testes BDD/E2E.
- Não há um "saga log" central; a rastreabilidade é feita por `eventId`/`correlationId` (= `osId`), logs estruturados (MDC) e métricas Micrometer em cada serviço.

## Tratamento de falhas e compensação

O OS Service implementa compensação por **evento compensatório + reversão de estado**, sem transações distribuídas:

| Evento de falha | Ação de compensação no OS Service | Evento publicado |
|---|---|---|
| `billing.quote.rejected.v1` / `billing.quote.failed.v1` (Billing) | OS transiciona para `CANCELADA` (rollback do estado `AGUARDANDO_APROVACAO`) | `os.cancelled` + `os.cancelled.v1` |
| `billing.payment.rejected.v1` / `billing.payment.failed.v1` (Billing) | OS transiciona para `CANCELADA` (rollback do estado `AGUARDANDO_PAGAMENTO`) | `os.cancelled` + `os.cancelled.v1` |
| `os.cancelled` (cancelamento externo) | OS transiciona para `CANCELADA` a partir de qualquer status não final | — |
| Falha na publicação de evento | Log de erro + métrica `os.events.published`/`failures`; a OS permanece persistida e o fluxo pode ser retomado por reprocessamento | — |
| Transição de status inválida | Rejeição com `IllegalStateException` (HTTP 400/500) — nenhuma alteração parcial é persistida | — |

Regras de compensação implementadas em `OrdemServicoService.processarEventoBilling`, `processarEventoPagamento`, `processarEventoSaga` e `cancelarOrdem`:

- **Reversão de estado**: a OS volta para `CANCELADA`, desfazendo o avanço no fluxo (ex.: `AGUARDANDO_APROVACAO` → `CANCELADA`).
- **Notificação downstream**: `os.cancelled` é publicado no `os.exchange` (Execution) e `os.cancelled.v1` no `mecanica.events` (Billing) para que os serviços interrompam ações parciais (estorno de orçamento, cancelamento de execução).
- **Idempotência por estado**: transições inválidas são rejeitadas, evitando compensações duplicadas.
- **Rastreabilidade**: cada transição é registrada em log estruturado (MDC) e em métricas Micrometer (`os.status.transitions`, contadores de sucesso/erro por operação).

Exemplos:

- `os.created` falha no Billing Service -> Billing não publica `billing.quote.submitted.v1`; a OS permanece em `RECEBIDA` até rejeição/cancelamento
- pagamento rejeitado/falho -> OS vai para `CANCELADA` com `os.cancelled` + `os.cancelled.v1`; a execução nunca é iniciada
- `os.approved` não chega ou é rejeitado -> execução não inicia; OS vai para `CANCELADA` com evento `os.cancelled`
- `execution.completed` não chega -> OS permanece em `EM_EXECUCAO`; compensação ocorre via cancelamento (`os.cancelled`) ou intervenção operacional

## Eventos implementados

`os.exchange` (OSEvent):

- `os.created`
- `os.approved`
- `os.status.updated`
- `os.cancelled`

Consumidos pelo OS Service no `os.exchange`:

- `execution.completed` (Execution -> OS)
- `os.cancelled` (cancelamento, inclusive o publicado pelo próprio OS)

`mecanica.events` (IntegrationEvent):

Produzidos pelo OS Service:

- `os.quote.requested.v1` (OS -> Billing)
- `os.cancelled.v1` (OS -> Billing)

Consumidos pelo OS Service (produzidos pelo Billing):

- `billing.quote.submitted.v1` (Billing -> OS)
- `billing.quote.approved.v1` (Billing -> OS)
- `billing.quote.rejected.v1` (Billing -> OS)
- `billing.quote.failed.v1` (Billing -> OS)
- `billing.payment.pending.v1` (Billing -> OS)
- `billing.payment.approved.v1` (Billing -> OS)
- `billing.payment.rejected.v1` (Billing -> OS)
- `billing.payment.failed.v1` (Billing -> OS)

> Os eventos `billing.payment.*.v1` fecham o ciclo de pagamento: a OS só vai para `EM_EXECUCAO` (e publica `os.approved`) após `billing.payment.approved.v1`.

## Observação

Este documento reflete a implementação real do OS Service e está alinhado com os contratos implementados pelo Billing Service e pelo Execution Service (ver `docs/contrato-eventos-saga.md` e `docs/integracao-microsservicos.md`).
