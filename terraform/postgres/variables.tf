variable "kubeconfig_path" {
  description = "Path to kubeconfig file"
  type        = string
  default     = "~/.kube/config"
}

variable "db_name" {
  description = "Name of the PostgreSQL database"
  type        = string
  default     = "os_service_db"
}

variable "db_username" {
  description = "PostgreSQL username"
  type        = string
  default     = "os_service_user"
}

variable "db_password" {
  description = "PostgreSQL password"
  type        = string
  sensitive   = true
  default     = "os_service_password"
}

variable "storage_class_name" {
  description = "Storage class for PostgreSQL PVC"
  type        = string
  default     = "standard"
}

variable "storage_size" {
  description = "Storage size for PostgreSQL"
  type        = string
  default     = "10Gi"
}
