variable "aws_region" {
  type        = string
  description = "Região AWS do backend."
}

variable "state_bucket_name" {
  type        = string
  description = "Nome globalmente único do bucket de estado."
}

variable "lock_table_name" {
  type        = string
  description = "Nome da tabela DynamoDB usada para locking."
}
