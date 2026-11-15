terraform {
  required_version = ">= 1.6.0"

  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "~> 5.0"
    }
    tls = {
      source = "hashicorp/tls"
    }
  }

  backend "s3" {}
}

provider "aws" {
  region = var.aws_region

  default_tags {
    tags = local.tags
  }
}

module "platform" {
  source = "../../modules/platform"

  name                        = "mecanica-os-production"
  environment                 = "production"
  aws_region                  = var.aws_region
  vpc_cidr                    = var.vpc_cidr
  az_count                    = var.az_count
  private_subnets             = var.private_subnets
  public_subnets              = var.public_subnets
  single_nat_gateway          = var.single_nat_gateway
  eks_version                 = var.eks_version
  node_instance_types         = var.node_instance_types
  node_capacity_type          = var.node_capacity_type
  node_min_size               = var.node_min_size
  node_max_size               = var.node_max_size
  node_desired_size           = var.node_desired_size
  ecr_repository_name         = "mecanica-os-service"
  create_shared_resources     = false
  ecr_image_retention         = var.ecr_image_retention
  runtime_secret_name         = "mecanica-os/production/runtime"
  secret_recovery_window_days = var.secret_recovery_window_days
  github_repository           = var.github_repository
  staging_namespace           = "os-service"
  production_namespace        = "os-service"
  tags                        = local.tags
}
