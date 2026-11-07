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
- Publica evento `os.created`

### 2. Geração do orçamento

- Billing Service recebe `os.created`
- Cria orçamento
- Aguarda aprovação do cliente ou do fluxo de negócio

### 3. Aprovação do orçamento

- Billing Service publica evento de aprovação ou rejeição
- OS Service atualiza a ordem conforme o status recebido
- Quando aprovado, a ordem passa para `EM_EXECUCAO`

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
| `orcamento.rejeitado` (Billing) | OS transiciona para `CANCELADA` (rollback do estado `AGUARDANDO_APROVACAO`) | `os.cancelled` |
| `os.cancelled` (cancelamento externo) | OS transiciona para `CANCELADA` a partir de qualquer status não final | — |
| Falha na publicação de evento | Log de erro + métrica `os.events.published`/`failures`; a OS permanece persistida e o fluxo pode ser retomado por reprocessamento | — |
| Transição de status inválida | Rejeição com `IllegalStateException` (HTTP 400/500) — nenhuma alteração parcial é persistida | — |

Regras de compensação implementadas em `OrdemServicoService.processarEventoSaga` e `cancelarOrdem`:

- **Reversão de estado**: a OS volta para `CANCELADA`, desfazendo o avanço no fluxo (ex.: `AGUARDANDO_APROVACAO` → `CANCELADA`).
- **Notificação downstream**: o evento `os.cancelled` é publicado no exchange `os.exchange` para que Billing e Execution interrompam ações parciais (estorno de orçamento, cancelamento de execução).
- **Idempotência por estado**: transições inválidas são rejeitadas, evitando compensações duplicadas.
- **Rastreabilidade**: cada transição é registrada em log estruturado (MDC) e em métricas Micrometer (`os.status.transitions`, contadores de sucesso/erro por operação).

Exemplos:

- `os.created` falha no Billing Service -> Billing não publica aprovação; a OS permanece em `AGUARDANDO_APROVACAO` até rejeição/cancelamento
- `os.approved` não chega ou é rejeitado -> execução não inicia; OS vai para `CANCELADA` com evento `os.cancelled`
- `execution.completed` não chega -> OS permanece em `EM_EXECUCAO`; compensação ocorre via cancelamento (`os.cancelled`) ou intervenção operacional

## Eventos sugeridos

- `os.created`
- `os.approved`
- `os.status.updated`
- `os.cancelled`
- `orçamento.aprovado`
- `orçamento.rejeitado`
- `execution.completed`

## Observação

Este documento documenta a estratégia correta para o challenge e o desenho que o grupo deve seguir para a atividade do Aluno 1.
