# Hafnium Terraform Infrastructure
# Skeleton module structure for cloud-agnostic infrastructure

terraform {
  required_version = ">= 1.5.0"

  required_providers {
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.25"
    }
    helm = {
      source  = "hashicorp/helm"
      version = "~> 2.12"
    }
  }

  backend "s3" {
    # Configure in environment-specific vars
    # bucket = "hafnium-terraform-state"
    # key    = "backend/terraform.tfstate"
    # region = "us-east-1"
  }
}

# Variables
variable "environment" {
  description = "Deployment environment (dev, staging, prod)"
  type        = string
  default     = "dev"
}

variable "cluster_name" {
  description = "Kubernetes cluster name"
  type        = string
  default     = "hafnium-cluster"
}

variable "namespace" {
  description = "Kubernetes namespace for Hafnium"
  type        = string
  default     = "hafnium"
}

variable "image_tag" {
  description = "Docker image tag to deploy"
  type        = string
  default     = "latest"
}

variable "replicas" {
  description = "Number of replicas for each service"
  type        = number
  default     = 2
}

# Kubernetes namespace
resource "kubernetes_namespace" "hafnium" {
  metadata {
    name = var.namespace

    labels = {
      name        = var.namespace
      environment = var.environment
      managed-by  = "terraform"
    }
  }
}

# Helm release for backend services
resource "helm_release" "hafnium_backend" {
  name       = "hafnium-backend"
  chart      = "${path.module}/../helm/hafnium-backend"
  namespace  = kubernetes_namespace.hafnium.metadata[0].name
  
  values = [
    yamlencode({
      global = {
        environment = var.environment
      }
      common = {
        replicaCount = var.replicas
      }
      services = {
        identity = {
          image = {
            tag = var.image_tag
          }
        }
        screening = {
          image = {
            tag = var.image_tag
          }
        }
        monitoring = {
          image = {
            tag = var.image_tag
          }
        }
        cases = {
          image = {
            tag = var.image_tag
          }
        }
        vault = {
          image = {
            tag = var.image_tag
          }
        }
        riskEngine = {
          image = {
            tag = var.image_tag
          }
        }
        signals = {
          image = {
            tag = var.image_tag
          }
        }
        apiFacade = {
          image = {
            tag = var.image_tag
          }
        }
      }
    })
  ]

  depends_on = [kubernetes_namespace.hafnium]
}

# Outputs
output "namespace" {
  description = "Kubernetes namespace"
  value       = kubernetes_namespace.hafnium.metadata[0].name
}

output "services" {
  description = "Deployed services"
  value = [
    "identity-service",
    "screening-service",
    "monitoring-service",
    "case-service",
    "vault-service",
    "risk-engine-service",
    "signals-service",
    "api-facade"
  ]
}
