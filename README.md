# OS Service - Microsserviço de Gestão de Ordens de Serviço

Microsserviço responsável por gerenciar as ordens de serviço (OS) do sistema de oficina mecânica, no contexto do Tech Challenge Fase 4 (arquitetura de microsserviços com Saga Pattern).

## Índice

- [Resumo da solução](#resumo-da-solução)
- [Decisão de arquitetura](#decisão-de-arquitetura)
- [Responsabilidades do serviço](#responsabilidades-do-serviço)
- [Stack tecnológica](#stack-tecnológica)
- [Estrutura do projeto](#estrutura-do-projeto)
- [Fluxo de Saga implementado](#fluxo-de-saga-implementado)
- [Arquitetura e Saga detalhada](docs/arquitetura-saga.md)
- [Contrato de eventos e payloads](docs/contrato-eventos-saga.md)
- [Integração com Billing e Execution](docs/integracao-microsservicos.md)
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

Observação importante: este repositório é o microsserviço OS Service, e não o ecossistema completo do challenge. A decisão de arquitetura abaixo foi feita para manter a solução correta e escalável dentro do conjunto de microsserviços do grupo.

---

## Decisão de arquitetura

### Recomendação adotada

- Banco relacional: PostgreSQL
- Mensageria: RabbitMQ
- Padrão de Saga: coreografia (via eventos RabbitMQ, sem orquestrador central)
- Comunicação: REST + eventos assíncronos
- NoSQL no conjunto do grupo: MongoDB em outro serviço (recomendado para o Billing Service ou outro microsserviço complementar)

### Por que esta escolha?

#### PostgreSQL
O domínio de OS exige consistência, histórico, transações e consultas por status; PostgreSQL é a melhor opção para esse cenário.

#### RabbitMQ
A comunicação assíncrona entre microsserviços é bem atendida pelo RabbitMQ, com baixo acoplamento e fácil integração com Spring Boot.

#### Saga por coreografia
O fluxo é coordenado de forma descentralizada por eventos: cada serviço executa sua transação local e reage aos eventos dos demais, sem orquestrador central. Isso reduz acoplamento, elimina ponto único de falha e mantém o OS Service como dono do ciclo de vida da OS (fonte da verdade do status). A implementação real segue essa estratégia — ver `docs/arquitetura-saga.md`.

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
| JUnit 5 | Testes unitários |
| Cucumber + JUnit 5 | Testes BDD |
| Maven | Build e dependências |
| Docker | Containerização |
| Kubernetes | Deploy |
| Terraform | Infraestrutura como código |
| Micrometer / Actuator | Observabilidade |
| SonarCloud | Qualidade de código no CI |
| Fortify on Demand | Análise de segurança no CI |
| GitHub Actions | Pipeline CI/CD |
| Amazon ECR | Registro de imagens Docker |
| Amazon EKS | Cluster Kubernetes |
| AWS Secrets Manager | Credenciais de runtime |
| GitHub OIDC | Autenticação AWS sem chaves estáticas |

---

## Estrutura do projeto

```text
mecanica-os-service/
├── .github/
│   └── workflows/
│       └── ci.yml
├── docs/
│   ├── arquitetura-saga.md
│   ├── contrato-eventos-saga.md
│   └── integracao-microsservicos.md
├── k8s/
│   ├── namespace.yaml
│   ├── configmap.yaml
│   ├── secret-contract.yaml
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
│   │   │   │   ├── OrdemServicoHistorico.java
│   │   │   │   └── StatusOrdemServico.java
│   │   │   ├── dto/
│   │   │   │   ├── CriarOrdemServicoDTO.java
│   │   │   │   ├── AtualizarStatusOSDTO.java
│   │   │   │   ├── OrdemServicoDTO.java
│   │   │   │   ├── StatusOrdemServicoDTO.java
│   │   │   │   ├── NotificacaoStatusDTO.java
│   │   │   │   ├── RespostaOrcamentoDTO.java
│   │   │   │   ├── OSEvent.java
│   │   │   │   └── billing/             # IntegrationEvent + payloads do Billing
│   │   │   ├── exception/
│   │   │   │   ├── ApiExceptionHandler.java
│   │   │   │   └── OrdemServicoNaoEncontradaException.java
│   │   │   ├── listener/
│   │   │   │   ├── BillingEventListener.java
│   │   │   │   └── OrdemServicoSagaListener.java
│   │   │   ├── mapper/
│   │   │   │   └── OrdemServicoMapper.java
│   │   │   ├── repository/
│   │   │   │   ├── OrdemServicoHistoricoRepository.java
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
│       │   ├── bdd/                  # Cucumber: os-saga.feature (contrato)
│       │   ├── e2e/                  # Cucumber E2E: os-saga-e2e.feature (serviços reais)
│       │   ├── config/
│       │   ├── controller/
│       │   ├── domain/
│       │   ├── dto/
│       │   ├── exception/
│       │   ├── listener/
│       │   ├── mapper/
│       │   ├── service/
│       │   ├── util/
│       │   └── validation/
│       └── resources/
│           ├── application-test.yaml
│           ├── features/
│           │   └── os-saga.feature
│           └── features-e2e/
│               └── os-saga-e2e.feature
├── e2e/                          # Teste E2E real (3 serviços + RabbitMQ + Postgres + Mongo)
│   ├── docker-compose.e2e.yml
│   └── run-e2e.sh
├── Dockerfile
├── docker-compose.yml
├── pom.xml
├── .env.example
├── postman/
│   └── os-service.postman_collection.json
├── terraform/
│   ├── README.md
│   ├── bootstrap/              # backend remoto (S3 + DynamoDB)
│   ├── environments/           # staging e production (EKS, ECR, Secrets Manager, OIDC)
│   ├── modules/platform/       # módulo compartilhado
│   └── postgres/               # PostgreSQL no cluster (StatefulSet + PVC + Service)
│       ├── README.md
│       ├── main.tf
│       ├── variables.tf
│       ├── outputs.tf
│       └── terraform.tfvars.example
├── README.md
└── .gitignore
```

### Sobre o pacote mapper

O projeto do monolito de referência usa `mapper` para separar conversão de entidade para DTO. Neste serviço, a conversão é simples e a lógica já está encapsulada em DTOs. Mesmo assim, adicionamos `OrdemServicoMapper` para manter o padrão da arquitetura do ecossistema e facilitar expansão futura.

---

## Fluxo de Saga implementado

### Estratégia escolhida: coreografia

A Saga é **coreografada** via eventos do RabbitMQ — não existe orquestrador central. Cada microsserviço executa sua transação local de forma independente e reage aos eventos publicados pelos demais. O OS Service é o dono do ciclo de vida da OS (máquina de estados em `StatusOrdemServico`), mas não comanda os outros serviços: ele publica fatos e consome eventos.

Fluxo principal (contratos reais implementados — ver `docs/contrato-eventos-saga.md`):

1. OS Service recebe a criação da OS e persiste em PostgreSQL (`RECEBIDA`).
2. Publica `os.created` no `os.exchange` e `os.quote.requested.v1` no `mecanica.events`.
3. Billing Service consome `os.quote.requested.v1`, gera e submete o orçamento.
4. Billing publica `billing.quote.submitted.v1` → OS vai para `AGUARDANDO_APROVACAO`.
5. Cliente aprova o orçamento via `POST /api/v1/quotes/{id}/approve` no Billing.
6. Billing publica `billing.quote.approved.v1` → OS vai para `AGUARDANDO_PAGAMENTO`.
7. Cliente paga via link do Mercado Pago; Billing confirma e publica `billing.payment.approved.v1` → OS vai para `EM_EXECUCAO` e publica `os.approved`.
8. Execution Service consome `os.approved` e executa o serviço.
9. Execution publica `execution.completed` → OS vai para `FINALIZADA` e publica `os.status.updated`.

Compensação: `billing.quote.rejected.v1`/`billing.quote.failed.v1`/`billing.payment.rejected.v1`/`billing.payment.failed.v1`/cancelamento → OS `CANCELADA` + `os.cancelled` (Execution) + `os.cancelled.v1` (Billing).

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

> **Nota:** o `spring.jpa.hibernate.ddl-auto` está configurado como `update` (adequado para o desafio e desenvolvimento). Em produção, recomenda-se `validate` ou migrações com Flyway/Liquibase.

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

### Consultar histórico de status

```http
GET /api/ordens-servico/{id}/historico
```

Retorna a trilha de auditoria da OS: cada registro contém `statusAnterior`, `statusNovo`, `origem` (`CRIACAO`, `ATUALIZACAO_MANUAL`, `ORCAMENTO:APROVADO`, `NOTIFICACAO_STATUS`, `CANCELAMENTO`, `SAGA:<evento>`), `motivo` (quando aplicável) e `criadoEm`.

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
mvn verify
```

O `mvn verify` executa testes, gera o relatório JaCoCo e valida a regra de cobertura configurada no `pom.xml` (100% de instruções, branches e linhas nas classes de lógica — entidades e DTOs gerados por Lombok são excluídos do check e do relatório).

Estado atual da cobertura:

- **280 testes** (unitários + BDD/Cucumber), 0 falhas (3 cenários E2E pulados sem os serviços no ar)
- **100% de instruções, branches e linhas** em todos os pacotes (`service`, `listener`, `controller`, `config`, `domain`, `dto` — incluindo `dto/billing` —, `exception`, `mapper`, `util`, `validation`)

Relatório: `target/site/jacoco/index.html`

### Testes BDD (Cucumber)

O serviço possui testes de comportamento com Cucumber + JUnit 5, executados junto com `mvn test`/`mvn verify` (não é preciso comando separado).

Para rodar apenas os cenários BDD:

```bash
mvn test -Dtest=RunCucumberTest
```

Arquivos:

- Steps: `src/test/java/com/f1rsters/os_service/bdd/OSServiceSagaSteps.java`
- Feature: `src/test/resources/features/os-saga.feature`

Cenários cobertos (5):

1. **Abertura, aprovação, pagamento e finalização** — OS criada (`RECEBIDA`) → orçamento gerado (`AGUARDANDO_APROVACAO`) → orçamento aprovado (`AGUARDANDO_PAGAMENTO`) → pagamento aprovado (`EM_EXECUCAO`) → execução concluída (`FINALIZADA`)
2. **Rejeição do orçamento** — orçamento rejeitado pelo Billing cancela a OS (`CANCELADA`), exercitando a compensação do Saga
3. **Aprovação, pagamento e execução via eventos** — fluxo completo dirigido por eventos do Saga: `billing.quote.approved.v1` → `billing.payment.approved.v1` (Billing, envelope `IntegrationEvent`) → `os.approved` (publicado) → `execution.completed` (Execution) → `os.status.updated` (publicado)
4. **Rejeição do pagamento** — pagamento rejeitado pelo Billing cancela a OS (`CANCELADA`)
5. **Cancelamento via evento** — evento `os.cancelled` recebido pelo listener transiciona a OS para `CANCELADA`

Os eventos dos serviços Billing e Execution são simulados nos steps invocando diretamente os métodos do `BillingEventListener` (envelope `IntegrationEvent` do contrato do Billing) e do `OrdemServicoSagaListener` (`OSEvent` do contrato do Execution), com `RabbitTemplate` mockado — validando o contrato de eventos definido em `docs/contrato-eventos-saga.md`, que está alinhado com o que Billing e Execution implementaram em suas branches `main`.

Existe ainda uma **suite BDD E2E** (`src/test/java/com/f1rsters/os_service/e2e/` + `src/test/resources/features-e2e/os-saga-e2e.feature`) que dirige os **3 microserviços reais** via REST e RabbitMQ, sem nenhum mock — ver seção de E2E abaixo.

### Teste E2E real (3 serviços)

A integração entre OS Service, Billing Service e Execution Service é validada de ponta a ponta com comunicação real via RabbitMQ, sem nenhum mock. O script `e2e/run-e2e.sh`:

1. Sobe a infraestrutura (RabbitMQ, PostgreSQL, MongoDB replica set `rs0`) via `e2e/docker-compose.e2e.yml`
2. Compila os 3 serviços (Billing e Execution são clonados de suas branches `main` para `/tmp/mecanica-e2e`)
3. Sobe os 3 serviços (OS na 8081, Billing na 8080, Execution na 8082)
4. Executa o fluxo Saga completo: criar OS → orçamento no Billing → aprovar orçamento → publicar pagamento aprovado → execução no Execution → OS `FINALIZADA`
5. Valida cada transição de status e a criação dos recursos nos serviços consumidores

```bash
bash e2e/run-e2e.sh          # build + execução completa (fluxo shell)
SKIP_BUILD=1 bash e2e/run-e2e.sh   # reutiliza jars já compilados
bash e2e/run-e2e.sh --keep  # mantém os serviços no ar ao final
bash e2e/run-e2e.sh --bdd   # executa o BDD E2E (Cucumber) contra os serviços reais
```

O modo `--bdd` sobe a mesma infraestrutura e os 3 serviços, e então roda a suite Cucumber E2E (`RunCucumberE2ETest`), que valida o fluxo completo do Saga com filas reais:

1. **Fluxo feliz** — OS criada → orçamento no Billing → aprovação → pagamento aprovado (evento publicado no RabbitMQ) → execução no Execution → OS `FINALIZADA`
2. **Rejeição do orçamento** — orçamento rejeitado no Billing → OS `CANCELADA`
3. **Rejeição do pagamento** — pagamento rejeitado (evento publicado no RabbitMQ) → OS `CANCELADA`
4. **Cancelamento via evento** — evento `os.cancelled` publicado na fila real do RabbitMQ → OS `CANCELADA` e execução `CANCELLED` no Execution (compensação real)

Sem os serviços no ar, os cenários E2E são **pulados** automaticamente (o `mvn verify` normal não quebra).

Fluxo validado: OS `RECEBIDA` → `AGUARDANDO_APROVACAO` → `AGUARDANDO_PAGAMENTO` → `EM_EXECUCAO` → `FINALIZADA`, com Quote criado no Billing e Execution criada no Execution Service, tudo via filas reais (`mecanica.events` e `os.exchange`). O evento `billing.payment.approved.v1` é publicado manualmente no E2E (não há stub do Mercado Pago).

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
- [x] Testes BDD (Cucumber) do fluxo Saga com eventos de Billing e Execution
- [x] Teste E2E real do fluxo Saga entre os 3 serviços (filas reais, sem mocks)
- [x] Cobertura com JaCoCo (100% instruções, branches e linhas nas classes verificadas)
- [x] Documentação inicial do projeto

### Requisitos que dependem do conjunto do grupo

- [ ] Banco NoSQL no conjunto da solução (Billing Service)
- [x] Fluxo completo de Saga com contratos alinhados aos serviços Billing e Execution (eventos versionados `mecanica.events` + `os.exchange`)
- [x] E2E real entre os 3 serviços no ar (`e2e/run-e2e.sh` — RabbitMQ + Postgres + Mongo)
- [ ] Swagger/Postman final do fluxo completo
- [ ] Deploy completo e monitoramento em ambiente integrado

### Decisão do grupo recomendada

- OS Service: PostgreSQL + RabbitMQ + Saga coreografada (dono do ciclo de vida da OS)
- Billing Service: MongoDB ou outro banco NoSQL para orçamento
- Execution Service: integração via eventos e status de execução

---

## Observações de projeto

- O serviço foi desenhado para ser independente e pronto para integração em um ecossistema de microsserviços.
- O pacote `mapper` foi adicionado para manter a padronização com a arquitetura do monolito de referência.
- O código foi preparado para facilitar evolução e manutenção sem deixar a solução "acoplada" ao banco do restante do sistema.
- A solução local foi validada com testes e com build Maven; a infraestrutura cloud (EKS/ECR/Secrets Manager) é provisionada via Terraform e aplicada pelo pipeline, sem credenciais AWS estáticas no GitHub.

---

O contrato de eventos (`docs/contrato-eventos-saga.md`) está alinhado com o que os serviços Billing e Execution implementaram em suas branches `main`, e o estado da integração está documentado em `docs/integracao-microsservicos.md`.

---

## Pipeline CI/CD

O workflow `.github/workflows/ci.yml` segue o mesmo padrão do `mecanica-execution-service` e executa:

1. **Build and Test** — `mvn verify` (testes + JaCoCo 100% instruction/branch/line) e upload dos relatórios como artifact
2. **Sonar and Fortify Analysis** — SonarCloud (`sonarqube-scan-action`, quality gate) e Fortify (`fortify-scan-action`)
3. **Build Docker Image** — somente na `main`: autenticação OIDC na AWS, build e push da imagem para o Amazon ECR (`vars.ECR_REPOSITORY`)
4. **Deploy staging** — somente na `main`: assume a role do environment `staging`, configura o `kubectl` para o EKS (`vars.EKS_CLUSTER_NAME`), lê o secret de runtime do AWS Secrets Manager (`vars.RUNTIME_SECRET_ID`), cria o Secret `os-service-secret` no cluster e aplica os manifests de `k8s/`
5. **Promote production** — somente na `main`, após o staging: mesmo fluxo usando o environment `production`

### Secrets e Variables do GitHub

**Repository secrets** (Settings → Secrets and variables → Actions → Secrets):

| Nome | Descrição |
|---|---|
| `SONAR_TOKEN` | Token de autenticação do SonarCloud |
| `FOD_CLIENT_ID` | Client ID (API key) do Fortify on Demand |
| `FOD_CLIENT_SECRET` | Client Secret (API secret) do Fortify on Demand |

**Repository variables** (aba Variables):

| Nome | Descrição |
|---|---|
| `SONAR_ORGANIZATION` | Organização no SonarCloud |
| `SONAR_PROJECT_KEY` | Chave do projeto no SonarCloud |
| `FOD_URL` | URL do tenant Fortify on Demand (ex: `https://ams.fortify.com`) — deve ser variable, não secret |
| `AWS_REGION` | Região da AWS (ex: `sa-east-1`) |
| `ECR_REPOSITORY` | Nome do repositório no Amazon ECR |

**Environment variables** (Settings → Environments → `staging` e `production`):

| Nome | Descrição |
|---|---|
| `AWS_ROLE_ARN` | ARN da role IAM do GitHub Actions para o ambiente |
| `EKS_CLUSTER_NAME` | Nome do cluster EKS do ambiente |
| `RUNTIME_SECRET_ID` | ID do secret no AWS Secrets Manager com as credenciais de runtime |

O secret de runtime (`mecanica-os/<ambiente>/runtime`) deve ser um JSON com as chaves `DATABASE_USERNAME`, `DATABASE_PASSWORD`, `RABBITMQ_HOST`, `RABBITMQ_PORT`, `RABBITMQ_USERNAME` e `RABBITMQ_PASSWORD` — ver `k8s/secret-contract.yaml`.

### Infraestrutura (Terraform)

- `terraform/bootstrap` — cria o backend remoto (bucket S3 + tabela DynamoDB)
- `terraform/environments/staging` e `terraform/environments/production` — provisionam VPC, EKS, ECR, Secrets Manager e a role OIDC do GitHub Actions (staging é o owner dos recursos compartilhados; aplique-o primeiro)
- `terraform/postgres` — provisiona o PostgreSQL dentro do cluster (StatefulSet + PVC + Service), ver `terraform/postgres/README.md`

Após o apply, publique os outputs `aws_region`, `ecr_repository`, `eks_cluster_name`, `runtime_secret_id` e `github_actions_role_arn` nas Variables/Environments do GitHub conforme a tabela acima.
