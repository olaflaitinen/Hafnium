# GKE Cluster
module "gke" {
  source = "./modules/gke"

  project_id     = var.project_id
  region         = var.region
  cluster_name   = var.gke_cluster_name
  node_count     = var.gke_node_count
  machine_type   = var.gke_machine_type
  environment    = var.environment
}

# Cloud SQL (PostgreSQL)
module "cloudsql" {
  source = "./modules/cloudsql"

  project_id  = var.project_id
  region      = var.region
  db_tier     = var.db_tier
  environment = var.environment

  depends_on = [module.gke]
}

# Memorystore Redis
module "redis" {
  source = "./modules/redis"

  project_id      = var.project_id
  region          = var.region
  memory_size_gb  = var.redis_memory_size_gb
  environment     = var.environment

  depends_on = [module.gke]
}

# Secret Manager
module "secrets" {
  source = "./modules/secret-manager"

  project_id  = var.project_id
  environment = var.environment
}

# Artifact Registry
resource "google_artifact_registry_repository" "backend" {
  location      = var.region
  repository_id = "hafnium-backend"
  description   = "Hafnium backend container images"
  format        = "DOCKER"
}
