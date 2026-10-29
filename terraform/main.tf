terraform {
  required_version = ">= 1.0"
  required_providers {
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.23"
    }
    helm = {
      source  = "hashicorp/helm"
      version = "~> 2.12"
    }
  }
}

provider "kubernetes" {
  config_path = var.kubeconfig_path
}

provider "helm" {
  kubernetes {
    config_path = var.kubeconfig_path
  }
}

# Namespace para o banco de dados
resource "kubernetes_namespace" "os_service_db" {
  metadata {
    name = "os-service-db"
  }
}

# PostgreSQL StatefulSet
resource "kubernetes_stateful_set" "postgres" {
  metadata {
    name      = "os-service-postgres"
    namespace = kubernetes_namespace.os_service_db.metadata[0].name
    labels = {
      app = "os-service-postgres"
    }
  }

  spec {
    service_name = "os-service-postgres"
    replicas     = 1

    selector {
      match_labels = {
        app = "os-service-postgres"
      }
    }

    template {
      metadata {
        labels = {
          app = "os-service-postgres"
        }
      }

      spec {
        container {
          name  = "postgres"
          image = "postgres:15-alpine"

          port {
            container_port = 5432
            name           = "postgres"
          }

          env {
            name  = "POSTGRES_DB"
            value = var.db_name
          }

          env {
            name  = "POSTGRES_USER"
            value = var.db_username
          }

          env {
            name  = "POSTGRES_PASSWORD"
            value_from {
              secret_key_ref {
                name = "os-service-postgres-secret"
                key  = "password"
              }
            }
          }

          env {
            name  = "PGDATA"
            value = "/var/lib/postgresql/data/pgdata"
          }

          volume_mount {
            name       = "postgres-storage"
            mount_path = "/var/lib/postgresql/data"
          }

          resources {
            limits = {
              cpu    = "1000m"
              memory = "1Gi"
            }
            requests = {
              cpu    = "250m"
              memory = "512Mi"
            }
          }
        }
      }
    }

    volume_claim_template {
      metadata {
        name = "postgres-storage"
      }

      spec {
        access_modes = ["ReadWriteOnce"]
        storage_class_name = var.storage_class_name

        resources {
          requests = {
            storage = var.storage_size
          }
        }
      }
    }
  }
}

# Secret para o PostgreSQL
resource "kubernetes_secret" "postgres_secret" {
  metadata {
    name      = "os-service-postgres-secret"
    namespace = kubernetes_namespace.os_service_db.metadata[0].name
  }

  data = {
    password = var.db_password
  }
}

# Service para o PostgreSQL
resource "kubernetes_service" "postgres" {
  metadata {
    name      = "os-service-postgres"
    namespace = kubernetes_namespace.os_service_db.metadata[0].name
  }

  spec {
    selector = {
      app = "os-service-postgres"
    }

    port {
      port        = 5432
      target_port = 5432
      protocol    = "TCP"
    }

    type = "ClusterIP"
  }
}

# ConfigMap para o OS Service com URL do banco
resource "kubernetes_config_map" "os_service_db_config" {
  metadata {
    name      = "os-service-db-config"
    namespace = "os-service"
  }

  data = {
    DATABASE_URL = "jdbc:postgresql://os-service-postgres.os-service-db.svc.cluster.local:5432/${var.db_name}"
  }
}
