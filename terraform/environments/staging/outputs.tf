output "aws_region" {
  value = module.platform.aws_region
}

output "ecr_repository" {
  value = module.platform.ecr_repository
}

output "ecr_repository_url" {
  value = module.platform.ecr_repository_url
}

output "eks_cluster_name" {
  value = module.platform.eks_cluster_name
}

output "runtime_secret_id" {
  value = module.platform.runtime_secret_id
}

output "github_actions_role_arn" {
  value = module.platform.github_actions_role_arn
}
