variable "aws_region" {
  type = string
}

variable "vpc_cidr" {
  type = string
}

variable "az_count" {
  type = number
}

variable "private_subnets" {
  type = list(string)
}

variable "public_subnets" {
  type = list(string)
}

variable "single_nat_gateway" {
  type = bool
}

variable "eks_version" {
  type = string
}

variable "node_instance_types" {
  type = list(string)
}

variable "node_capacity_type" {
  type = string
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

variable "ecr_image_retention" {
  type = number
}

variable "secret_recovery_window_days" {
  type = number
}

variable "github_repository" {
  type = string
}

locals {
  tags = {
    Project     = "mecanica"
    Service     = "os"
    Environment = "staging"
    ManagedBy   = "terraform"
  }
}
