# Hafnium Terraform Infrastructure

This directory contains Terraform configurations for provisioning cloud infrastructure.

## Modules

| Module | Description |
|--------|-------------|
| `modules/networking` | VPC, subnets, security groups |
| `modules/kubernetes` | EKS/GKE/AKS cluster |
| `modules/database` | RDS PostgreSQL |
| `modules/messaging` | MSK/Redpanda |
| `modules/observability` | Prometheus, Grafana, Loki |

## Prerequisites

- Terraform 1.5+
- Cloud provider CLI configured
- Backend state storage configured

## Directory Structure

```
terraform/
├── modules/
│   ├── networking/
│   ├── kubernetes/
│   ├── database/
│   ├── messaging/
│   └── observability/
├── environments/
│   ├── development/
│   ├── staging/
│   └── production/
├── README.md
└── versions.tf
```

## Usage

### Initialize

```bash
cd environments/development
terraform init
```

### Plan

```bash
terraform plan -out=tfplan
```

### Apply

```bash
terraform apply tfplan
```

### Destroy

```bash
terraform destroy
```

## State Management

State is stored remotely in encrypted S3/GCS with state locking.

```hcl
terraform {
  backend "s3" {
    bucket         = "hafnium-terraform-state"
    key            = "env/development/terraform.tfstate"
    region         = "us-east-1"
    encrypt        = true
    dynamodb_table = "hafnium-terraform-locks"
  }
}
```

## Security Considerations

- Secrets managed via Vault or cloud secrets manager
- State encryption enabled
- Least-privilege IAM roles
- Network isolation via security groups

---

**DISCLAIMER**: Infrastructure configurations are provided as reference implementations. Production deployments require security and compliance review.
