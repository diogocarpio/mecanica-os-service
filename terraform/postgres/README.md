# Terraform - Infraestrutura do Banco de Dados OS Service

Este diretório contém a infraestrutura como código (Terraform) para provisionar o banco de dados PostgreSQL do OS Service no Kubernetes.

## Pré-requisitos

- Terraform >= 1.0
- kubectl configurado
- Cluster Kubernetes local (Kind, Minikube, K3d) ou remoto

## Como usar

### 1. Inicializar o Terraform

```bash
terraform init
```

### 2. Configurar variáveis

Copie o arquivo de exemplo:

```bash
cp terraform.tfvars.example terraform.tfvars
```

Edite o arquivo `terraform.tfvars` com suas configurações.

### 3. Validar a configuração

```bash
terraform validate
```

### 4. Planejar as mudanças

```bash
terraform plan
```

### 5. Aplicar a infraestrutura

```bash
terraform apply
```

### 6. Destruir a infraestrutura

```bash
terraform destroy
```

## Recursos criados

- **Namespace**: `os-service-db`
- **StatefulSet**: PostgreSQL 15 com 1 réplica
- **Service**: ClusterIP para acesso interno
- **Secret**: Credenciais do banco
- **PVC**: Volume persistente (10Gi por padrão)
- **ConfigMap**: Configuração do DATABASE_URL para o OS Service

## Variáveis

| Variável | Descrição | Padrão |
|----------|-----------|--------|
| `kubeconfig_path` | Path do kubeconfig | `~/.kube/config` |
| `db_name` | Nome do banco | `os_service_db` |
| `db_username` | Usuário do banco | `os_service_user` |
| `db_password` | Senha do banco | `os_service_password` |
| `storage_class_name` | Storage class | `standard` |
| `storage_size` | Tamanho do volume | `10Gi` |

## Outputs

- `postgres_service_name`: Nome do serviço PostgreSQL
- `postgres_namespace`: Namespace do PostgreSQL
- `postgres_host`: Host do PostgreSQL
- `postgres_port`: Porta do PostgreSQL
- `database_url`: URL JDBC completa
