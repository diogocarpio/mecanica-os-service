# OS Service - Microsserviço de Gestão de Ordens de Serviço

Microsserviço responsável por gerenciar as ordens de serviço (OS) do sistema de oficina mecânica. Este serviço é parte integrante da arquitetura de microserviços do Tech Challenge Fase 4.

## Responsabilidades

- Abertura de ordens de serviço
- Atualização do status das ordens de serviço
- Consulta do status e histórico das ordens de serviço
- Integração com mensageria para comunicação com outros microserviços
- Publicação de eventos para orquestração do Saga Pattern

## Tecnologias Utilizadas

| Tecnologia | Versão | Descrição |
|------------|--------|-----------|
| **Java** | 17 | Linguagem principal |
| **Spring Boot** | 3.2.0 | Framework para construção da API REST |
| **Spring Data JPA** | - | Persistência de dados com Hibernate |
| **Spring Boot Actuator** | - | Health checks e monitoramento |
| **Spring AMQP** | - | Integração com RabbitMQ |
| **PostgreSQL** | 15 | Banco de dados relacional |
| **H2 Database** | - | Banco de dados em memória para testes |
| **RabbitMQ** | 3.x | Mensageria para comunicação assíncrona |
| **Lombok** | - | Redução de boilerplate no código |
| **SpringDoc OpenAPI** | 2.3.0 | Documentação Swagger/OpenAPI |
| **JaCoCo** | 0.8.11 | Cobertura de testes |
| **Maven** | - | Gerenciamento de dependências e build |
| **Docker** | - | Containerização da aplicação |
| **Kubernetes** | - | Orquestração de containers |
| **Terraform** | >= 1.0 | Infraestrutura como código |

## Arquitetura do Serviço

```
Controller (REST)  -->  Service (Regras de Negócio)  -->  Repository (JPA)  -->  Database
     |                       |                              |
     v                       v                              v
    DTO                   Domain (Entidades)          PostgreSQL
     |
     v
RabbitMQ (Event Publisher)
```

### Componentes

- **Controller**: Recebe as requisições HTTP e delega para os services
- **Service**: Contém a lógica de negócio e publicação de eventos
- **Repository**: Interface JPA para acesso ao banco de dados
- **Domain**: Entidades JPA mapeadas para tabelas do banco
- **DTO**: Objetos de transferência para entrada e saída de dados
- **Validation**: Validadores customizados para CPF/CNPJ e Placa
- **Exception**: Tratamento global de exceções da API
- **RabbitMQ**: Publicação de eventos para integração com outros serviços

## Estrutura de Pastas

```
mecanica-os-service/
├── src/
│   ├── main/
│   │   ├── java/com/f1rsters/os_service/
│   │   │   ├── OsServiceApplication.java          # Classe principal
│   │   │   ├── config/
│   │   │   │   ├── OpenApiConfig.java             # Configuração Swagger/OpenAPI
│   │   │   │   └── RabbitMQConfig.java            # Configuração RabbitMQ
│   │   │   ├── controller/
│   │   │   │   └── OrdemServicoController.java     # Endpoints REST
│   │   │   ├── domain/
│   │   │   │   ├── OrdemServico.java              # Entidade Ordem de Serviço
│   │   │   │   └── StatusOrdemServico.java        # Enum de status
│   │   │   ├── dto/
│   │   │   │   ├── CriarOrdemServicoDTO.java      # DTO de criação
│   │   │   │   ├── AtualizarStatusOSDTO.java      # DTO de atualização
│   │   │   │   ├── OrdemServicoDTO.java           # DTO de resposta
│   │   │   │   ├── StatusOrdemServicoDTO.java     # DTO de status
│   │   │   │   ├── NotificacaoStatusDTO.java      # DTO de notificação
│   │   │   │   ├── RespostaOrcamentoDTO.java      # DTO de orçamento
│   │   │   │   └── OSEvent.java                   # DTO de evento RabbitMQ
│   │   │   ├── exception/
│   │   │   │   └── ApiExceptionHandler.java       # Handler global de exceções
│   │   │   ├── repository/
│   │   │   │   └── OrdemServicoRepository.java    # Repository JPA
│   │   │   ├── service/
│   │   │   │   └── OrdemServicoService.java       # Service de negócio
│   │   │   ├── util/
│   │   │   │   ├── InputNormalizer.java          # Normalização de input
│   │   │   │   ├── CpfCnpjValidator.java         # Validador de CPF/CNPJ
│   │   │   │   ├── PlacaValidator.java           # Validador de placa
│   │   │   │   └── SensitiveDataMasker.java      # Mascaramento de dados
│   │   │   └── validation/
│   │   │       ├── CpfCnpjValido.java            # Anotação @CpfCnpjValido
│   │   │       ├── CpfCnpjConstraintValidator.java
│   │   │       ├── PlacaValida.java              # Anotação @PlacaValida
│   │   │       └── PlacaConstraintValidator.java
│   │   └── resources/
│   │       └── application.yaml                   # Configuração principal
│   └── test/
│       ├── java/com/f1rsters/os_service/          # Testes unitários
│       └── resources/
│           └── application-test.yaml              # Configuração para testes
├── k8s/                                            # Manifestos Kubernetes
│   ├── namespace.yaml                              # Namespace do serviço
│   ├── configmap.yaml                              # ConfigMap não sensível
│   ├── secret.yaml                                 # Secret sensível
│   ├── deployment.yaml                              # Deployment do serviço
│   ├── service.yaml                                # Service do serviço
│   └── hpa.yaml                                    # Horizontal Pod Autoscaler
├── terraform/                                       # Infraestrutura do banco
│   ├── main.tf                                     # Configuração principal
│   ├── variables.tf                                # Variáveis Terraform
│   ├── outputs.tf                                  # Outputs Terraform
│   ├── terraform.tfvars.example                    # Exemplo de variáveis
│   └── README.md                                   # Documentação Terraform
├── .github/workflows/                              # Pipelines CI/CD
│   └── ci-cd.yml                                   # Workflow GitHub Actions
├── Dockerfile                                      # Imagem Docker
├── pom.xml                                         # Dependências Maven
└── README.md                                       # Este arquivo
```

## Pré-requisitos

### Para execução local

- [Java JDK 17](https://adoptium.net/) (Eclipse Temurin recomendado)
- [Maven 3.8+](https://maven.apache.org/download.cgi)
- [PostgreSQL 15](https://www.postgresql.org/download/)
- [RabbitMQ 3.x](https://www.rabbitmq.com/download.html)
- [Git](https://git-scm.com/downloads)

### Para Docker

- [Docker](https://docs.docker.com/get-docker/) (versão 20+)
- [Docker Compose](https://docs.docker.com/compose/install/) (versão 2+)

### Para Kubernetes

- [kubectl](https://kubernetes.io/docs/tasks/tools/)
- Cluster Kubernetes local (Kind, Minikube ou K3d)

### Para Terraform

- [Terraform](https://developer.hashicorp.com/terraform/downloads) >= 1.0
- Cluster Kubernetes configurado

## Configuração e Execução

### Opção 1 - Execução Local com PostgreSQL e RabbitMQ

**1. Clone o repositório:**

```bash
git clone https://github.com/diogocarpio/mecanica-os-service.git
cd mecanica-os-service
```

**2. Configure o PostgreSQL:**

Crie o banco de dados e o usuário no PostgreSQL:

```sql
CREATE DATABASE os_service_db;
CREATE USER os_service_user WITH PASSWORD 'os_service_password';
GRANT ALL PRIVILEGES ON DATABASE os_service_db TO os_service_user;
```

**3. Configure o RabbitMQ:**

Certifique-se de que o RabbitMQ está rodando na porta padrão (5672) com as credenciais padrão (guest/guest).

**4. Execute a aplicação:**

```bash
# Linux / macOS
./mvnw spring-boot:run

# Windows
mvnw.cmd spring-boot:run
```

A aplicação iniciará na porta `8081` e o Hibernate criará as tabelas automaticamente (`ddl-auto: update`).

**5. Acesse a API:**
- API: `http://localhost:8081`
- Swagger UI: `http://localhost:8081/swagger-ui/index.html`
- Actuator Health: `http://localhost:8081/actuator/health`

### Opção 2 - Docker Compose

**1. Clone o repositório:**

```bash
git clone https://github.com/diogocarpio/mecanica-os-service.git
cd mecanica-os-service
```

**2. Suba os containers:**

```bash
docker-compose up --build -d
```

**3. Verifique se os containers estão rodando:**

```bash
docker-compose ps
```

**4. Acesse a API:**
- API: `http://localhost:8081`
- Swagger UI: `http://localhost:8081/swagger-ui/index.html`

**5. Para parar os containers:**

```bash
docker-compose down
```

### Opção 3 - Kubernetes

**1. Build da imagem Docker:**

```bash
docker build -t os-service:latest .
```

**2. Carregar a imagem no cluster (se usando Kind):**

```bash
kind load docker-image os-service:latest
```

**3. Provisionar o banco de dados com Terraform:**

```bash
cd terraform
terraform init
terraform plan
terraform apply
```

**4. Aplicar os manifestos Kubernetes:**

```bash
kubectl apply -f k8s/namespace.yaml
kubectl apply -f k8s/configmap.yaml
kubectl apply -f k8s/secret.yaml
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/service.yaml
kubectl apply -f k8s/hpa.yaml
```

**5. Verificar os recursos:**

```bash
kubectl get pods -n os-service
kubectl get svc -n os-service
kubectl get hpa -n os-service
```

**6. Acessar a aplicação:**

```bash
# Obter a porta do LoadBalancer
kubectl get svc os-service -n os-service

# Acessar via LoadBalancer
http://localhost:<PORT>
```

**7. Remover os recursos:**

```bash
kubectl delete -f k8s/
cd terraform && terraform destroy
```

## Endpoints da API

### Criar Ordem de Serviço

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

### Atualizar Status

```http
PATCH /api/ordens-servico/{id}/status
Content-Type: application/json

{
  "novoStatus": "EM_EXECUCAO"
}
```

### Listar Todas as Ordens

```http
GET /api/ordens-servico
```

### Detalhar Ordem

```http
GET /api/ordens-servico/{id}
```

### Consultar Status

```http
GET /api/ordens-servico/{id}/status
```

### Responder Orçamento

```http
POST /api/ordens-servico/{id}/orcamento/resposta
Content-Type: application/json

{
  "aprovado": true
}
```

### Notificar Status (Webhook)

```http
POST /api/ordens-servico/{id}/status/notificacao
Content-Type: application/json

{
  "novoStatus": "FINALIZADA"
}
```

## Integração com Mensageria (RabbitMQ)

O OS Service publica eventos para integração com outros microserviços:

### Eventos Publicados

- **os.created**: Publicado nova OS é criada (Billing Service deve gerar orçamento)
- **os.approved**: Publicado quando orçamento é aprovado (Execution Service deve iniciar execução)
- **os.status.updated**: Publicado quando status é atualizado (notificações e monitoramento)

### Estrutura do Evento

```json
{
  "osId": 1,
  "clienteId": "12345678909",
  "veiculoId": "ABC1234",
  "servicosIds": ["1", "2"],
  "pecasIds": ["1"],
  "valorTotal": 500.00,
  "status": "RECEBIDA",
  "eventType": "os.created",
  "timestamp": "2024-01-01T10:00:00"
}
```

### Filas e Exchanges

- **Exchange**: `os.exchange` (topic)
- **Queues**:
  - `os.created.queue`
  - `os.approved.queue`
  - `os.status.updated.queue`

## Saga Pattern

O OS Service participa do Saga Pattern para orquestração do fluxo de ordens de serviço:

### Fluxo de Saga (Coreografia)

1. **OS Service**: Cria OS → Publica evento `os.created`
2. **Billing Service**: Recebe evento → Gera orçamento → Aguarda aprovação
3. **Cliente**: Aprova/rejeita orçamento via Billing Service
4. **Billing Service**: Se aprovado → Publica evento `orçamento.approved`
5. **OS Service**: Recebe evento → Atualiza status para `EM_EXECUCAO` → Publica `os.approved`
6. **Execution Service**: Recebe evento → Inicia execução → Atualiza status durante processo
7. **Execution Service**: Finaliza execução → Publica `execution.completed`
8. **OS Service**: Recebe evento → Atualiza status para `FINALIZADA`

### Compensação (Rollback)

Se falhar em qualquer etapa:
- **OS Service**: Cancela OS → Publica `os.cancelled`
- **Billing Service**: Recebe `os.cancelled` → Cancela orçamento (se existente)
- **Execution Service**: Recebe `os.cancelled` → Interrompe execução (se em andamento)

## Testes

### Executar Testes

```bash
mvn test
```

### Executar com Cobertura

```bash
mvn test jacoco:report
```

O relatório de cobertura será gerado em `target/site/jacoco/index.html`.

### Verificar Cobertura Mínima (80%)

```bash
mvn jacoco:check
```

## CI/CD

O pipeline CI/CD é automatizado via GitHub Actions e inclui:

### Jobs

1. **Build and Test**: Compilação, execução de testes e verificação de cobertura (mínimo 80%)
2. **Docker Build**: Build da imagem Docker e verificação de health check
3. **Quality Check**: Análise de qualidade com SonarQube (opcional)
4. **Deploy to Kubernetes**: Deploy automático no cluster (branch main)
5. **Terraform Apply**: Provisionamento da infraestrutura do banco (branch main)

### Proteção de Branch

- Branch `main` protegida (sem commits diretos)
- Pull Requests obrigatórios
- Checagens automatizadas obrigatórias antes do merge

## Monitoramento

### Endpoints do Actuator

- `/actuator/health`: Health check
- `/actuator/info`: Informações da aplicação
- `/actuator/metrics`: Métricas Prometheus
- `/actuator/prometheus`: Métricas no formato Prometheus

### Métricas Coletadas

- `ordem_servico.criar_ordem.success.total`: Total de OS criadas com sucesso
- `ordem_servico.criar_ordem.unexpected_error.total`: Erros na criação de OS
- `ordem_servico.atualizar_status.success.total`: Total de atualizações de status com sucesso
- `ordem_servico.status.transition.total`: Total de transições de status
- `ordem_servico.processing.failure.total`: Falhas no processamento
- `ordem_servico.status.lead_time.seconds`: Tempo para alcançar cada status

## Variáveis de Ambiente

| Variável | Descrição | Padrão |
|----------|-----------|--------|
| `DATABASE_URL` | URL JDBC do PostgreSQL | `jdbc:postgresql://localhost:5432/os_service_db` |
| `DATABASE_USERNAME` | Usuário do banco | `os_service_user` |
| `DATABASE_PASSWORD` | Senha do banco | `os_service_password` |
| `RABBITMQ_HOST` | Host do RabbitMQ | `localhost` |
| `RABBITMQ_PORT` | Porta do RabbitMQ | `5672` |
| `RABBITMQ_USERNAME` | Usuário do RabbitMQ | `guest` |
| `RABBITMQ_PASSWORD` | Senha do RabbitMQ | `guest` |
| `SERVER_PORT` | Porta da aplicação | `8081` |

## Documentação Swagger

A documentação interativa da API está disponível via Swagger UI:

```
http://localhost:8081/swagger-ui/index.html
```

## Postman Collection

Uma coleção do Postman com todos os endpoints está disponível no repositório (a ser criada).

## Contribuição

Este é um projeto acadêmico do Tech Challenge FIAP Fase 4. Para contribuições:

1. Faça um fork do repositório
2. Crie uma branch para sua feature (`git checkout -b feature/nova-feature`)
3. Commit suas mudanças (`git commit -am 'Adiciona nova feature'`)
4. Push para a branch (`git push origin feature/nova-feature`)
5. Abra um Pull Request

## Licença

Este projeto está sob a licença Apache 2.0.

## Contato

- **Aluno 1**: [Seu Nome]
- **Repositório**: https://github.com/diogocarpio/mecanica-os-service

## Links Relacionados

- [Billing Service](https://github.com/diogocarpio/mecanica-billing-service)
- [Execution Service](https://github.com/diogocarpio/mecanica-execution-service)
- [Monolito Original (Fase 3)](https://github.com/diogocarpio/f1rsters-tech-challenge-mecanica)
