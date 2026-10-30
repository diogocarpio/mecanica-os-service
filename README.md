# OS Service - Microsserviço de Gestão de Ordens de Serviço

Microsserviço responsável por gerenciar as ordens de serviço (OS) do sistema de oficina mecânica. Este projeto foi estruturado para cumprir a responsabilidade do Aluno 1 do Tech Challenge Fase 4.

## Índice

- [Resumo da solução](#resumo-da-solução)
- [Decisão de arquitetura](#decisão-de-arquitetura)
- [Responsabilidades do serviço](#responsabilidades-do-serviço)
- [Stack tecnológica](#stack-tecnológica)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Fluxo de Saga recomendado](#fluxo-de-saga-recomendado)
- [Arquitetura e Saga detalhada](docs/arquitetura-saga.md)
- [Contrato de eventos e payloads](docs/contrato-eventos-saga.md)
- [Requisitos e configurações](#requisitos-e-configurações)
- [Configuração local](#configuração-local)
- [Executando a aplicação](#executando-a-aplicação)
- [Variáveis de ambiente](#variáveis-de-ambiente)
- [Endpoints principais](#endpoints-principais)
- [Swagger/OpenAPI](#swaggeropenapi)
- [Coleção Postman](postman/os-service.postman_collection.json)
- [Arquivo de ambiente](.env.example)
- [Teste e cobertura](#teste-e-cobertura)
- [Checklist de alinhamento com o challenge](#checklist-de-alinhamento-com-o-challenge)
- [Observações de projeto](#observações-de-projeto)

---

## Resumo da solução

Este serviço implementa o núcleo do fluxo de ordens de serviço:

- abertura da OS
- atualização do status
- consulta do status e do histórico
- publicação de eventos em RabbitMQ para integração com outros microsserviços
- tratamento de erros com logs, métricas e observabilidade

A solução foi pensada para manter o serviço independente, com banco de dados próprio, API REST e integração assíncrona com outros serviços do ecossistema.

Observação importante: este repositório é o microsserviço OS Service, e não o ecossistema completo do challenge. A decisão de arquitetura abaixo foi feita para manter a solução correta e escalável para o Aluno 1.

---

## Decisão de arquitetura

### Recomendação adotada

- Banco relacional: PostgreSQL
- Mensageria: RabbitMQ
- Padrão de Saga: orquestração
- Comunicação: REST + eventos assíncronos
- NoSQL no conjunto do grupo: MongoDB em outro serviço (recomendado para o Billing Service ou outro microsserviço complementar)

### Por que esta escolha?

#### PostgreSQL
O domínio de OS exige consistência, histórico, transações e consultas por status; PostgreSQL é a melhor opção para esse cenário.

#### RabbitMQ
A comunicação assíncrona entre microsserviços é bem atendida pelo RabbitMQ, com baixo acoplamento e fácil integração com Spring Boot.

#### Saga por orquestração
Para o desafio acadêmico, orquestração é mais simples de explicar, documentar e validar. O serviço centraliza a sequência de ações e facilita o tratamento de falhas e compensação.

#### NoSQL no conjunto da solução
O projeto do challenge exige, no conjunto da arquitetura, pelo menos um banco NoSQL. A recomendação é manter o NoSQL fora do OS Service, em um serviço de orçamento ou outro componente de apoio.

---

## Responsabilidades do serviço

- Abertura de ordens de serviço
- Atualização de status da OS
- Consulta do status e do histórico da ordem
- Publicação e consumo de eventos de integração
- Persistência do ciclo de vida da OS em PostgreSQL
- Exposição de métricas, healthcheck e logs de operação

---

## Stack tecnológica

| Tecnologia | Versão / Uso |
|---|---|
| Java | 17 |
| Spring Boot | 3.2.0 |
| Spring Web | API REST |
| Spring Data JPA | Persistência |
| PostgreSQL | Banco relacional |
| RabbitMQ | Mensageria |
| Spring AMQP | Integração com RabbitMQ |
| H2 Database | Testes |
| SpringDoc OpenAPI | Swagger |
| Lombok | Redução de boilerplate |
| JaCoCo | Cobertura de testes |
| Maven | Build e dependências |
| Docker | Containerização |
| Kubernetes | Deploy |
| Terraform | Infraestrutura como código |
| Micrometer / Actuator | Observabilidade |

---

## Estrutura do projeto

```text
mecanica-os-service/
├── .github/
│   └── workflows/
│       └── ci-cd.yml
├── docs/
│   └── (documentação complementar, se adicionada no futuro)
├── k8s/
│   ├── namespace.yaml
│   ├── configmap.yaml
│   ├── secret.yaml
│   ├── deployment.yaml
│   ├── service.yaml
│   └── hpa.yaml
├── src/
│   ├── main/
│   │   ├── java/com/f1rsters/os_service/
│   │   │   ├── config/
│   │   │   │   ├── OpenApiConfig.java
│   │   │   │   └── RabbitMQConfig.java
│   │   │   ├── controller/
│   │   │   │   └── OrdemServicoController.java
│   │   │   ├── domain/
│   │   │   │   ├── OrdemServico.java
│   │   │   │   └── StatusOrdemServico.java
│   │   │   ├── dto/
│   │   │   │   ├── CriarOrdemServicoDTO.java
│   │   │   │   ├── AtualizarStatusOSDTO.java
│   │   │   │   ├── OrdemServicoDTO.java
│   │   │   │   ├── StatusOrdemServicoDTO.java
│   │   │   │   ├── NotificacaoStatusDTO.java
│   │   │   │   ├── RespostaOrcamentoDTO.java
│   │   │   │   └── OSEvent.java
│   │   │   ├── exception/
│   │   │   │   └── ApiExceptionHandler.java
│   │   │   ├── mapper/
│   │   │   │   └── OrdemServicoMapper.java
│   │   │   ├── repository/
│   │   │   │   └── OrdemServicoRepository.java
│   │   │   ├── service/
│   │   │   │   └── OrdemServicoService.java
│   │   │   ├── util/
│   │   │   │   ├── InputNormalizer.java
│   │   │   │   ├── CpfCnpjValidator.java
│   │   │   │   ├── PlacaValidator.java
│   │   │   │   └── SensitiveDataMasker.java
│   │   │   ├── validation/
│   │   │   │   ├── CpfCnpjValido.java
│   │   │   │   ├── CpfCnpjConstraintValidator.java
│   │   │   │   ├── PlacaValida.java
│   │   │   │   └── PlacaConstraintValidator.java
│   │   │   └── OsServiceApplication.java
│   │   └── resources/
│   │       └── application.yaml
│   └── test/
│       ├── java/com/f1rsters/os_service/
│       │   ├── config/
│       │   ├── controller/
│       │   ├── exception/
│       │   ├── mapper/
│       │   ├── service/
│       │   └── util/
│       └── resources/
│           └── application-test.yaml
├── Dockerfile
├── pom.xml
├── terraform/
│   ├── README.md
│   ├── main.tf
│   ├── variables.tf
│   ├── outputs.tf
│   └── terraform.tfvars.example
├── README.md
└── .gitignore
```

### Sobre o pacote mapper

O projeto do monolito de referência usa `mapper` para separar conversão de entidade para DTO. Neste serviço, a conversão é simples e a lógica já está encapsulada em DTOs. Mesmo assim, adicionamos `OrdemServicoMapper` para manter o padrão da arquitetura do ecossistema e facilitar expansão futura.

---

## Fluxo de Saga recomendado

### Estratégia escolhida: orquestração

A saga será orquestrada pelo OS Service e pelos serviços de apoio do ecossistema.

Fluxo principal:

1. OS Service recebe a criação da OS.
2. Persiste a OS em PostgreSQL.
3. Publica evento `os.created` no RabbitMQ.
4. Billing Service recebe o evento e gera o orçamento.
5. Cliente aprova ou rejeita o orçamento.
6. Billing Service publica aprovação/rejeição.
7. OS Service recebe a aprovação e atualiza a OS para `EM_EXECUCAO`.
8. Execution Service recebe `os.approved` e inicia a execução.
9. Quando finaliza, publica status de conclusão.
10. OS Service atualiza o estado final da ordem.

### Compensação / rollback

Se houver falha no orçamento ou na execução:

- OS Service registra a falha
- marca a ordem como cancelada ou rejeitada
- publica evento de compensação
- os serviços participantes encerram atividades pendentes

Esse padrão mantém o fluxo distribuído controlado e documentável para o Tech Challenge.

---

## Requisitos e configurações

### Pré-requisitos

- Java 17+
- Maven 3.8+
- PostgreSQL 15+
- RabbitMQ 3.x
- Docker (opcional)
- Kubernetes (opcional)

### Sem AWS

Este projeto não executa ações de AWS, não usa AWS CLI e não faz provisionamento em cloud. O foco aqui é o microsserviço local e o desenho de arquitetura conforme o challenge.

---

## Configuração local

### 1) Criar o banco PostgreSQL

```sql
CREATE DATABASE os_service_db;
CREATE USER os_service_user WITH PASSWORD 'os_service_password';
GRANT ALL PRIVILEGES ON DATABASE os_service_db TO os_service_user;
```

### 2) Iniciar o RabbitMQ

Certifique-se de que o RabbitMQ esteja rodando localmente na porta 5672 com usuário/senha padrão:

- Usuário: `guest`
- Senha: `guest`

### 3) Verificar a aplicação

A aplicação usa as seguintes variáveis padrão se não forem definidas:

- `DATABASE_URL=jdbc:postgresql://localhost:5432/os_service_db`
- `DATABASE_USERNAME=os_service_user`
- `DATABASE_PASSWORD=os_service_password`
- `RABBITMQ_HOST=localhost`
- `RABBITMQ_PORT=5672`
- `RABBITMQ_USERNAME=guest`
- `RABBITMQ_PASSWORD=guest`
- `SERVER_PORT=8081`

---

## Executando a aplicação

### Opção 1: executar localmente com Maven

```bash
cd mecanica-os-service
mvn spring-boot:run
```

Aplicação disponível em:

- API: `http://localhost:8081`
- Swagger UI: `http://localhost:8081/swagger-ui/index.html`
- Healthcheck: `http://localhost:8081/actuator/health`

### Opção 2: executar via Docker

Use o arquivo `docker-compose.yml` do projeto para subir em conjunto o PostgreSQL, o RabbitMQ e a própria aplicação:

```bash
cp .env.example .env

docker compose up --build -d
```

Ou, se preferir rodar a imagem manualmente:

```bash
docker build -t os-service:latest .
docker run --rm -p 8081:8081 --name os-service os-service:latest
```

### Opção 3: executar os testes

```bash
mvn test
```

### Opção 4: gerar relatório de cobertura

```bash
mvn test jacoco:report
```

Relatório: `target/site/jacoco/index.html`

---

## Variáveis de ambiente

| Variável | Descrição | Valor padrão |
|---|---|---|
| `DATABASE_URL` | URL do PostgreSQL | `jdbc:postgresql://localhost:5432/os_service_db` |
| `DATABASE_USERNAME` | Usuário do banco | `os_service_user` |
| `DATABASE_PASSWORD` | Senha do banco | `os_service_password` |
| `RABBITMQ_HOST` | Host do RabbitMQ | `localhost` |
| `RABBITMQ_PORT` | Porta do RabbitMQ | `5672` |
| `RABBITMQ_USERNAME` | Usuário do RabbitMQ | `guest` |
| `RABBITMQ_PASSWORD` | Senha do RabbitMQ | `guest` |
| `SERVER_PORT` | Porta da API | `8081` |

---

## Endpoints principais

### Criar ordem de serviço

```http
POST /api/ordens-servico
Content-Type: application/json

{
  "cpfCnpjCliente": "12345678909",
  "placaVeiculo": "ABC1234",
  "servicosIds": ["1", "2"],
  "pecasIds": ["1"]
}
```

### Atualizar status

```http
PATCH /api/ordens-servico/{id}/status
Content-Type: application/json

{
  "novoStatus": "EM_EXECUCAO"
}
```

### Listar ordens

```http
GET /api/ordens-servico
```

### Detalhar ordem

```http
GET /api/ordens-servico/{id}
```

### Consultar status

```http
GET /api/ordens-servico/{id}/status
```

### Responder orçamento

```http
POST /api/ordens-servico/{id}/orcamento/resposta
Content-Type: application/json

{
  "aprovado": true
}
```

### Notificar status

```http
POST /api/ordens-servico/{id}/status/notificacao
Content-Type: application/json

{
  "novoStatus": "FINALIZADA"
}
```

---

## Swagger/OpenAPI

A aplicação expõe a documentação interativa pelo SpringDoc OpenAPI.

Acesse:

```text
http://localhost:8081/swagger-ui/index.html
```

Também há uma coleção base para Postman em:

```text
postman/os-service.postman_collection.json
```

Ela pode ser importada diretamente no Postman para testar a API localmente.

---

## Teste e cobertura

### Rodar testes

```bash
mvn test
```

### Gerar relatório JaCoCo

```bash
mvn jacoco:report
```

### Verificar regra de cobertura

```bash
mvn jacoco:check
```

A cobertura é validada com JaCoCo na pipeline e os testes do serviço cobrem a lógica de criação, atualização, consulta, validação, mapeamento e tratamento de exceções.

---

## Checklist de alinhamento com o challenge

### Requisitos atendidos pelo microsserviço OS Service

- [x] Banco de dados próprio
- [x] Infraestrutura própria
- [x] Dockerfile
- [x] Kubernetes manifests
- [x] Pipeline CI/CD
- [x] API REST
- [x] Estrutura de eventos via RabbitMQ
- [x] Testes unitários
- [x] Cobertura com JaCoCo
- [x] Documentação inicial do projeto

### Requisitos que dependem do conjunto do grupo

- [ ] Banco NoSQL no conjunto da solução
- [ ] Fluxo completo de Saga integrado entre microsserviços
- [ ] BDD de integração entre serviços
- [ ] Swagger/Postman final do fluxo completo
- [ ] Deploy completo e monitoramento em ambiente integrado

### Decisão do grupo recomendada

- OS Service: PostgreSQL + RabbitMQ + orquestração de saga
- Billing Service: MongoDB ou outro banco NoSQL para orçamento
- Execution Service: integração via eventos e status de execução

---

## Observações de projeto

- O serviço foi desenhado para ser independente e pronto para integração em um ecossistema de microsserviços.
- O pacote `mapper` foi adicionado para manter a padronização com a arquitetura do monolito de referência.
- O código foi preparado para facilitar evolução e manutenção sem deixar a solução "acoplada" ao banco do restante do sistema.
- A solução local foi validada com testes e com build Maven; não houve uso de AWS CLI nem qualquer provisionamento em cloud.

---

Se quiser, o próximo passo é alinhar o desenho completo com os outros microsserviços do grupo, definindo os eventos exatos, o banco NoSQL e os nomes finais dos serviços de Billing e Execution.
