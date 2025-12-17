# Hafnium Networking Module

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
  description = "Kubernetes namespace"
  type        = string
}

# Default deny all ingress/egress
resource "kubernetes_network_policy" "default_deny" {
  metadata {
    name      = "default-deny-all"
    namespace = var.namespace
  }

  spec {
    pod_selector {}

    policy_types = ["Ingress", "Egress"]
  }
}

# Allow DNS egress
resource "kubernetes_network_policy" "allow_dns" {
  metadata {
    name      = "allow-dns"
    namespace = var.namespace
  }

  spec {
    pod_selector {}

    policy_types = ["Egress"]

    egress {
      ports {
        protocol = "UDP"
        port     = "53"
      }
      ports {
        protocol = "TCP"
        port     = "53"
      }
    }
  }
}

# Backend services internal communication
resource "kubernetes_network_policy" "backend_internal" {
  metadata {
    name      = "backend-internal"
    namespace = var.namespace
  }

  spec {
    pod_selector {
      match_labels = {
        tier = "backend"
      }
    }

    policy_types = ["Ingress", "Egress"]

    ingress {
      from {
        pod_selector {
          match_labels = {
            tier = "backend"
          }
        }
      }
      from {
        pod_selector {
          match_labels = {
            app = "api-gateway"
          }
        }
      }
      ports {
        protocol = "TCP"
        port     = "8080"
      }
    }

    egress {
      to {
        pod_selector {
          match_labels = {
            tier = "backend"
          }
        }
      }
      to {
        pod_selector {
          match_labels = {
            app = "postgres"
          }
        }
      }
      to {
        pod_selector {
          match_labels = {
            app = "redis"
          }
        }
      }
      to {
        pod_selector {
          match_labels = {
            app = "kafka"
          }
        }
      }
    }
  }
}
