# Contrato de Eventos e Saga do OS Service

## Visão geral

Este documento define o contrato dos eventos usados no fluxo de ordens de serviço entre os microsserviços do grupo. O objetivo é manter a integração desacoplada, rastreável e compatível com o padrão Saga.

## Estratégia adotada

Padrão Saga por orquestração.

O OS Service atua como ponto central da ordem de serviço e coordena o fluxo principal:

1. Criação da OS
2. Geração do orçamento
3. Aprovação do orçamento
4. Execução do serviço
5. Compensação em caso de falha

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

### 1) os.created

- Publicado por: OS Service
- Consumido por: Billing Service
- Quando: após a criação da OS
- Descrição: comunica que a ordem foi aberta e pode receber orçamento

Payload:

```json
{
  "osId": 1,
  "clienteId": "10000000000",
  "veiculoId": "ABC1234",
  "servicosIds": ["1", "2"],
  "pecasIds": ["10"],
  "valorTotal": 0.00,
  "status": "RECEBIDA",
  "eventType": "os.created",
  "timestamp": "2026-10-30T00:00:00"
}
```

Ação esperada:
- Billing Service gera orçamento
- publica `orcamento.aprovado` ou `orcamento.rejeitado`

---

### 2) orcamento.aprovado

- Publicado por: Billing Service
- Consumido por: OS Service
- Quando: orçamento aprovado
- Descrição: indica que a OS pode seguir para execução

Payload:

```json
{
  "osId": 1,
  "clienteId": "10000000000",
  "veiculoId": "ABC1234",
  "servicosIds": ["1", "2"],
  "pecasIds": ["10"],
  "valorTotal": 250.00,
  "status": "AGUARDANDO_APROVACAO",
  "eventType": "orcamento.aprovado",
  "timestamp": "2026-10-30T00:03:00"
}
```

Ação esperada no OS Service:
- atualiza o status da OS para `EM_EXECUCAO`
- publica `os.approved`

---

### 3) orcamento.rejeitado

- Publicado por: Billing Service
- Consumido por: OS Service
- Quando: orçamento recusado ou rejeitado
- Descrição: indica falha no fluxo de aprovação

Payload:

```json
{
  "osId": 1,
  "clienteId": "10000000000",
  "veiculoId": "ABC1234",
  "servicosIds": ["1", "2"],
  "pecasIds": ["10"],
  "valorTotal": 250.00,
  "status": "AGUARDANDO_APROVACAO",
  "eventType": "orcamento.rejeitado",
  "timestamp": "2026-10-30T00:04:00"
}
```

Ação esperada:
- OS Service marca a ordem como `CANCELADA`
- publica `os.cancelled`

---

### 4) os.approved

- Publicado por: OS Service
- Consumido por: Execution Service
- Quando: a aprovação do orçamento foi processada
- Descrição: sinaliza que a etapa de aprovação foi concluída e a execução pode iniciar

Payload:

```json
{
  "osId": 1,
  "clienteId": "10000000000",
  "veiculoId": "ABC1234",
  "servicosIds": ["1", "2"],
  "pecasIds": ["10"],
  "valorTotal": 250.00,
  "status": "EM_EXECUCAO",
  "eventType": "os.approved",
  "timestamp": "2026-10-30T00:05:00"
}
```

Ação esperada:
- Execution Service inicia o serviço

---

### 5) execution.completed

- Publicado por: Execution Service
- Consumido por: OS Service
- Quando: o serviço foi executado com sucesso
- Descrição: conclui a ordem de serviço

Payload:

```json
{
  "osId": 1,
  "clienteId": "10000000000",
  "veiculoId": "ABC1234",
  "servicosIds": ["1", "2"],
  "pecasIds": ["10"],
  "valorTotal": 250.00,
  "status": "FINALIZADA",
  "eventType": "execution.completed",
  "timestamp": "2026-10-30T00:20:00"
}
```

Ação esperada no OS Service:
- atualiza a OS para `FINALIZADA`
- publica `os.status.updated`

---

### 6) os.cancelled

- Publicado por: OS Service ou serviço de compensação
- Consumido por: Billing Service / Execution Service / outros serviços dependentes
- Quando: ocorre falha no fluxo e precisa compensação
- Descrição: cancela a operação distribuída

Payload:

```json
{
  "osId": 1,
  "clienteId": "10000000000",
  "veiculoId": "ABC1234",
  "servicosIds": ["1", "2"],
  "pecasIds": ["10"],
  "valorTotal": 250.00,
  "status": "CANCELADA",
  "eventType": "os.cancelled",
  "timestamp": "2026-10-30T00:07:00"
}
```

Ação esperada:
- Billing cancela o orçamento em processamento
- Execution interrompe execução em andamento
- OS Service mantém a ordem em `CANCELADA`

---

### 7) os.status.updated

- Publicado por: OS Service
- Consumido por: monitoramento / outros serviços de integração
- Quando: qualquer status sofre alteração relevante
- Descrição: notificação do ciclo de vida da ordem

Payload:

```json
{
  "osId": 1,
  "clienteId": "10000000000",
  "veiculoId": "ABC1234",
  "servicosIds": ["1", "2"],
  "pecasIds": ["10"],
  "valorTotal": 250.00,
  "status": "FINALIZADA",
  "eventType": "os.status.updated",
  "timestamp": "2026-10-30T00:21:00"
}
```

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

---

## Compensação e rollback

Quando ocorre falha em alguma etapa do fluxo, o sistema deve:

1. identificar a falha
2. registrar motivo logado e com métrica
3. atualizar a OS para `CANCELADA`
4. publicar `os.cancelled`
5. permitir que o restante do ecossistema encerre ou reverta ações pendentes

Fluxos de compensação esperados:

- falha no orçamento: `orcamento.rejeitado` -> `os.cancelled`
- falha na execução: `execution.failed` ou evento equivalente -> `os.cancelled`
- falha de integração: status em `CANCELADA` e compensação do serviço dependente

---

## Observações para o grupo

- O contrato acima deve ser compartilhado com o Billing Service e Execution Service.
- O payload pode crescer conforme a necessidade, mas os campos mínimos acima devem ser preservados.
- O `eventType` deve ser obrigatório para facilitar rastreio e observabilidade.
- O `timestamp` deve ser preenchido em todos os eventos.
- O `osId` deve ser o identificador da ordem de serviço para correlação entre serviços.

---

## Resumo executivo

O contrato aqui definido garante que:

- a ordem de serviço é rastreada em todo o fluxo
- todos os serviços sabem qual evento foi publicado
- a transação distribuída pode ser compensada em caso de falha
- o OS Service mantém o controle do ciclo de vida da ordem

Este contrato é o ponto de alinhamento entre o OS Service, o Billing Service e o Execution Service.
