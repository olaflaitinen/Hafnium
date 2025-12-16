variable "project_id" {
  type = string
}

variable "region" {
  type = string
}

variable "db_tier" {
  type = string
}

variable "environment" {
  type = string
}

resource "google_sql_database_instance" "main" {
  name             = "hafnium-${var.environment}"
  database_version = "POSTGRES_16"
  region           = var.region

  settings {
    tier              = var.db_tier
    availability_type = var.environment == "prod" ? "REGIONAL" : "ZONAL"

    backup_configuration {
      enabled                        = true
      point_in_time_recovery_enabled = true
      start_time                     = "03:00"
      transaction_log_retention_days = 7

      backup_retention_settings {
        retained_backups = 30
        retention_unit   = "COUNT"
      }
    }

    ip_configuration {
      ipv4_enabled    = false
      private_network = "projects/${var.project_id}/global/networks/hafnium-cluster-vpc"
    }

    database_flags {
      name  = "log_checkpoints"
      value = "on"
    }

    database_flags {
      name  = "log_connections"
      value = "on"
    }

    database_flags {
      name  = "log_disconnections"
      value = "on"
    }
  }

  deletion_protection = var.environment == "prod"
}

resource "google_sql_database" "hafnium" {
  name     = "hafnium"
  instance = google_sql_database_instance.main.name
}

resource "google_sql_user" "hafnium" {
  name     = "hafnium"
  instance = google_sql_database_instance.main.name
  password = random_password.db_password.result
}

resource "random_password" "db_password" {
  length  = 32
  special = false
}

resource "google_secret_manager_secret_version" "db_password" {
  secret      = google_secret_manager_secret.db_password.id
  secret_data = random_password.db_password.result
}

resource "google_secret_manager_secret" "db_password" {
  secret_id = "hafnium-db-password-${var.environment}"

  replication {
    auto {}
  }
}

output "connection_name" {
  value = google_sql_database_instance.main.connection_name
}

output "database_name" {
  value = google_sql_database.hafnium.name
}
