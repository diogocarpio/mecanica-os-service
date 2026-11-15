# Terraform

Esta camada provisiona a infraestrutura AWS do serviço:

- VPC multi-AZ e subnets para EKS;
- cluster EKS, node group e addons essenciais;
- repositório ECR;
- segredo de runtime no Secrets Manager, sem versionar valores;
- OIDC do GitHub Actions e role de CI/CD;
- integração de acesso da role ao EKS.

MongoDB e RabbitMQ são externos. O Terraform cria somente o contrato do segredo
no Secrets Manager; o valor deve ser preenchido por um processo seguro fora do
código e do pipeline de infraestrutura.

## Ambientes

Cada ambiente possui estado separado em `environments/staging` e
`environments/production`. Aplique staging primeiro: ele é o owner dos recursos
compartilhados ECR e GitHub OIDC; production consulta esses recursos. Copie
`terraform.tfvars.example`, configure os
valores locais/seguros e passe o backend remoto no `terraform init`:

```powershell
terraform -chdir=terraform/environments/staging init -backend-config=backend.hcl
terraform -chdir=terraform/environments/staging plan -var-file=terraform.tfvars
terraform -chdir=terraform/environments/staging apply -var-file=terraform.tfvars
```

O bucket S3 e a tabela DynamoDB do backend devem ser criados primeiro pelo
bootstrap em `terraform/bootstrap`. O bootstrap usa estado local inicialmente;
proteja o arquivo de variáveis e não coloque credenciais no repositório.

Após o apply, publique os outputs `aws_region`, `ecr_repository`,
`eks_cluster_name`, `runtime_secret_id` e `github_actions_role_arn` nas
Variables dos GitHub Environments/shared variables já esperadas pelo workflow.

O Terraform não aplica a imagem da aplicação nem os overlays Kustomize. O
workflow existente continua responsável por publicar a imagem, ler o segredo de
runtime e executar o deploy em staging/production. O job de publicação usa o
Environment `staging` para assumir a role que possui acesso ao ECR compartilhado.
