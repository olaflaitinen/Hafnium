variable "project_id" {
  type = string
}

variable "region" {
  type = string
}

variable "memory_size_gb" {
  type = number
}

variable "environment" {
  type = string
}

resource "google_redis_instance" "main" {
  name           = "hafnium-redis-${var.environment}"
  tier           = var.environment == "prod" ? "STANDARD_HA" : "BASIC"
  memory_size_gb = var.memory_size_gb
  region         = var.region

  authorized_network = "projects/${var.project_id}/global/networks/hafnium-cluster-vpc"

  redis_version = "REDIS_7_0"

  auth_enabled            = true
  transit_encryption_mode = "SERVER_AUTHENTICATION"

  maintenance_policy {
    weekly_maintenance_window {
      day = "SUNDAY"
      start_time {
        hours   = 3
        minutes = 0
      }
    }
  }
}

output "host" {
  value = google_redis_instance.main.host
}

output "port" {
  value = google_redis_instance.main.port
}
