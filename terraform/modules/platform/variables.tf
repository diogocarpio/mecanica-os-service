variable "name" {
  type = string
}

variable "environment" {
  type = string
}

variable "aws_region" {
  type = string
}

variable "vpc_cidr" {
  type = string
}

variable "az_count" {
  type    = number
  default = 3
}

variable "private_subnets" {
  type = list(string)
}

variable "public_subnets" {
  type = list(string)
}

variable "single_nat_gateway" {
  type    = bool
  default = false
}

variable "eks_version" {
  type = string
}

variable "node_instance_types" {
  type = list(string)
}

variable "node_capacity_type" {
  type    = string
  default = "ON_DEMAND"
}

variable "node_min_size" {
  type = number
}

variable "node_max_size" {
  type = number
}

variable "node_desired_size" {
  type = number
}

variable "ecr_repository_name" {
  type = string
}

variable "create_shared_resources" {
  type        = bool
  description = "Cria ECR e OIDC compartilhados. Apenas um ambiente deve ser o owner."
  default     = false
}

variable "ecr_image_retention" {
  type    = number
  default = 30
}

variable "runtime_secret_name" {
  type = string
}

variable "secret_recovery_window_days" {
  type    = number
  default = 7
}

variable "github_repository" {
  type        = string
  description = "owner/repository usado no subject do OIDC."
}

variable "staging_namespace" {
  type    = string
  default = "os-service"
}

variable "production_namespace" {
  type    = string
  default = "os-service"
}

variable "tags" {
  type    = map(string)
  default = {}
}
