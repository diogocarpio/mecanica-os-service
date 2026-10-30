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

Se houver erro em qualquer etapa:

- a ordem é marcada como cancelada ou rejeitada
- o serviço de origem publica evento de compensação
- serviços dependentes interrompem ou revertam ações parciais

Exemplos:

- `os.created` falha no Billing Service -> ordem fica em status de falha e o fluxo é encerrado
- `os.approved` não chega ou é rejeitado -> execução não inicia
- `execution.completed` não chega -> OS permanece em status de execução até compensação ou retriagem

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
