output "aws_region" {
  value = var.aws_region
}

output "ecr_repository" {
  value = local.ecr_repository_name
}

output "ecr_repository_url" {
  value = local.ecr_repository_url
}

output "eks_cluster_name" {
  value = module.eks.cluster_name
}

output "eks_cluster_arn" {
  value = module.eks.cluster_arn
}

output "runtime_secret_id" {
  value = aws_secretsmanager_secret.runtime.id
}

output "github_actions_role_arn" {
  value = aws_iam_role.github_actions.arn
}

output "vpc_id" {
  value = module.vpc.vpc_id
}
