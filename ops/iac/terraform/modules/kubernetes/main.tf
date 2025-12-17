# Hafnium Kubernetes Cluster Module

terraform {
  required_version = ">= 1.5.0"

  required_providers {
    kubernetes = {
      source  = "hashicorp/kubernetes"
      version = "~> 2.25"
    }
  }
}

variable "namespace" {
  description = "Kubernetes namespace for Hafnium"
  type        = string
  default     = "hafnium"
}

variable "environment" {
  description = "Deployment environment"
  type        = string
  validation {
    condition     = contains(["dev", "staging", "prod"], var.environment)
    error_message = "Environment must be dev, staging, or prod."
  }
}

variable "replicas" {
  description = "Default replica count"
  type        = number
  default     = 2
}

# Namespace
resource "kubernetes_namespace" "hafnium" {
  metadata {
    name = var.namespace

    labels = {
      "app.kubernetes.io/name"       = "hafnium"
      "app.kubernetes.io/managed-by" = "terraform"
      "environment"                  = var.environment
      "pod-security.kubernetes.io/enforce" = "restricted"
      "pod-security.kubernetes.io/audit"   = "restricted"
    }
  }
}

# Service Account
resource "kubernetes_service_account" "hafnium" {
  metadata {
    name      = "hafnium-service"
    namespace = kubernetes_namespace.hafnium.metadata[0].name
  }
}

# Resource Quota
resource "kubernetes_resource_quota" "hafnium" {
  metadata {
    name      = "hafnium-quota"
    namespace = kubernetes_namespace.hafnium.metadata[0].name
  }

  spec {
    hard = {
      "requests.cpu"    = var.environment == "prod" ? "32" : "8"
      "requests.memory" = var.environment == "prod" ? "64Gi" : "16Gi"
      "limits.cpu"      = var.environment == "prod" ? "64" : "16"
      "limits.memory"   = var.environment == "prod" ? "128Gi" : "32Gi"
      "pods"            = var.environment == "prod" ? "100" : "50"
    }
  }
}

# Limit Range
resource "kubernetes_limit_range" "hafnium" {
  metadata {
    name      = "hafnium-limits"
    namespace = kubernetes_namespace.hafnium.metadata[0].name
  }

  spec {
    limit {
      type = "Container"
      default = {
        cpu    = "500m"
        memory = "512Mi"
      }
      default_request = {
        cpu    = "100m"
        memory = "128Mi"
      }
      max = {
        cpu    = "4"
        memory = "8Gi"
      }
    }
  }
}

output "namespace" {
  value = kubernetes_namespace.hafnium.metadata[0].name
}

output "service_account" {
  value = kubernetes_service_account.hafnium.metadata[0].name
}
