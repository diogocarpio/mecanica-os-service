output "postgres_service_name" {
  description = "Name of the PostgreSQL service"
  value       = kubernetes_service.postgres.metadata[0].name
}

output "postgres_namespace" {
  description = "Namespace of PostgreSQL"
  value       = kubernetes_namespace.os_service_db.metadata[0].name
}

output "postgres_host" {
  description = "PostgreSQL host"
  value       = "${kubernetes_service.postgres.metadata[0].name}.${kubernetes_namespace.os_service_db.metadata[0].name}.svc.cluster.local"
}

output "postgres_port" {
  description = "PostgreSQL port"
  value       = kubernetes_service.postgres.spec[0].port[0].port
}

output "database_url" {
  description = "JDBC URL for the database"
  value       = "jdbc:postgresql://${kubernetes_service.postgres.metadata[0].name}.${kubernetes_namespace.os_service_db.metadata[0].name}.svc.cluster.local:5432/${var.db_name}"
  sensitive   = true
}
