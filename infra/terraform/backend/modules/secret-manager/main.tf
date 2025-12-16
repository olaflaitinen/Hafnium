variable "project_id" {
  type = string
}

variable "environment" {
  type = string
}

locals {
  secret_keys = [
    "hafnium-jwt-secret",
    "hafnium-vault-encryption-key",
    "hafnium-vault-hmac-key",
    "hafnium-keycloak-client-secret"
  ]
}

resource "google_secret_manager_secret" "app_secrets" {
  for_each  = toset(local.secret_keys)
  secret_id = "${each.value}-${var.environment}"

  replication {
    auto {}
  }
}

resource "google_project_iam_member" "secret_accessor" {
  project = var.project_id
  role    = "roles/secretmanager.secretAccessor"
  member  = "serviceAccount:hafnium-backend@${var.project_id}.iam.gserviceaccount.com"
}
