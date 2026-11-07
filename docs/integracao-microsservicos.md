# Integração Billing e Execution — Spec para fechar o BDD

Este documento define **o mínimo que cada microsserviço precisa implementar** para que o fluxo Saga funcione de ponta a ponta e o BDD do OS Service possa rodar contra os serviços reais.

> Contrato de eventos completo: `docs/contrato-eventos-saga.md`

---

## Infraestrutura comum (os 3 serviços)

Todos devem usar a **mesma topologia RabbitMQ**:

- **Exchange**: `os.exchange` (TopicExchange)
- **MessageConverter**: `Jackson2JsonMessageConverter` com `ObjectMapper` registrado com `JavaTimeModule` (obrigatório por causa do `LocalDateTime timestamp`)
- **Payload**: mesmo formato do `OSEvent` (ver contrato) — campos mínimos: `osId`, `clienteId`, `veiculoId`, `servicosIds`, `pecasIds`, `valorTotal`, `status`, `eventType`, `timestamp`

---

## Aluno 2 — Billing Service (`mecanica-billing-service`)

### O que construir

1. **Consumir `os.created`**
   - Fila: `os.created.queue` (já declarada pelo OS Service)
   - Routing key: `os.created`
   - Listener chama a geração de orçamento

2. **Gerar orçamento**
   - Entidade `Orcamento`: `id`, `osId`, `clienteId`, `veiculoId`, `servicosIds`, `pecasIds`, `valorTotal`, `status` (`PENDENTE`/`APROVADO`/`REJEITADO`/`CANCELADO`), `criadoEm`
   - Banco: MongoDB (conforme README) ou PostgreSQL
   - Endpoint REST para o cliente aprovar/rejeitar:
     - `POST /api/orcamentos/{osId}/aprovar`
     - `POST /api/orcamentos/{osId}/rejeitar`

3. **Publicar resultado do orçamento** (após aprovação/rejeição do cliente)
   - Aprovado → routing key `orcamento.aprovado` → fila `billing.orcamento.aprovado.queue`
   - Rejeitado → routing key `orcamento.rejeitado` → fila `billing.orcamento.rejeitado.queue`
   - Payload: mesmo `OSEvent` com `eventType` correspondente e `valorTotal` preenchido

4. **Compensação**
   - Consumir `os.cancelled` (routing key `os.cancelled`) → marcar orçamento como `CANCELADO` (estorno lógico)

### Critério de pronto

- Criar OS no OS Service → orçamento aparece no Billing
- Aprovar orçamento → OS vai para `EM_EXECUCAO`
- Rejeitar orçamento → OS vai para `CANCELADA`

---

## Aluno 3 — Execution Service (`mecanica-execution-service`)

### O que construir

1. **Consumir `os.approved`**
   - Fila: `os.approved.queue` (já declarada pelo OS Service)
   - Routing key: `os.approved`
   - Listener cria a execução

2. **Gerenciar execução**
   - Entidade `Execucao`: `id`, `osId`, `status` (`EM_ANDAMENTO`/`CONCLUIDA`/`FALHA`/`CANCELADA`), `criadoEm`, `concluidoEm`
   - Banco: PostgreSQL (conforme README)
   - Simular execução: pode ser imediata ou agendada (ex.: `@Scheduled` ou delay) — para o BDD, o ideal é um endpoint para forçar conclusão ou um delay curto (5–10s)

3. **Publicar conclusão**
   - Routing key `execution.completed` → fila `execution.completed.queue`
   - Payload: `OSEvent` com `status: "FINALIZADA"` e `eventType: "execution.completed"`

4. **Compensação**
   - Consumir `os.cancelled` → marcar execução como `CANCELADA` se ainda em andamento

### Critério de pronto

- OS aprovada → execução criada no Execution
- Execução concluída → OS vai para `FINALIZADA`
- OS cancelada durante execução → execução cancelada

---

## Como fechar o BDD (teste de integração real)

Com os 3 serviços implementados, o BDD do OS Service pode ser promovido de **simulado** para **real**:

### Opção A — Teste de integração no OS Service (recomendado)

Criar um perfil de teste `integration` no OS Service que:

1. Sobe RabbitMQ real (Testcontainers ou docker-compose)
2. Sobe Billing e Execution (ou mocks WireMock dos endpoints REST deles)
3. O Cucumber chama o REST do OS Service e **deixa os eventos fluírem de verdade** pelo RabbitMQ
4. O step `Then o status da OS deve ser "FINALIZADA"` passa a verificar o resultado da interação real

### Opção B — Teste E2E no repositório do grupo/orquestrador

Um repositório separado (ou o do orquestrador) com docker-compose subindo os 3 serviços + RabbitMQ + Postgres + Mongo, e um script/Cucumber validando o fluxo completo:

```text
POST OS → os.created → Billing gera orçamento → aprova → orcamento.aprovado
→ OS EM_EXECUCAO → os.approved → Execution executa → execution.completed
→ OS FINALIZADA
```

### O que já está pronto no OS Service

- Filas e bindings declarados (`RabbitMQConfig`)
- Listener dos 4 eventos de entrada (`OrdemServicoSagaListener`)
- Publicação dos 4 eventos de saída (`os.created`, `os.approved`, `os.status.updated`, `os.cancelled`)
- BDD com os cenários do fluxo (hoje simulando os eventos; basta trocar o step para não chamar o listener diretamente quando os serviços reais estiverem no ar)

---

## Pendência conhecida no contrato

O contrato menciona `execution.failed` como evento de falha da execução, mas o OS Service **ainda não possui listener** para ele. Se o Execution Service for implementar falhas, alinhar com o OS Service:

- Opção 1: Execution publica `execution.failed` → OS Service adiciona listener e transiciona para `CANCELADA`
- Opção 2: Execution publica `os.cancelled` diretamente (já suportado)

Recomendado: **Opção 2** para não exigir mudança no OS Service.
