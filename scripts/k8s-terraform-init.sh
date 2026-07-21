#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
TF_DIR="$PROJECT_ROOT/infra/terraform"

echo "=== Resume Builder: Terraform Infrastructure Init ==="

# Ensure k3s is running
if ! kubectl cluster-info &>/dev/null; then
  echo "Error: k3s cluster not reachable. Run scripts/k8s-start.sh first."
  exit 1
fi

# Check for tfvars
if [ ! -f "$TF_DIR/terraform.tfvars" ]; then
  echo "No terraform.tfvars found. Copying from example..."
  cp "$TF_DIR/terraform.tfvars.example" "$TF_DIR/terraform.tfvars"
  echo "Please edit $TF_DIR/terraform.tfvars before continuing."
  exit 1
fi

cd "$TF_DIR"

echo "Running terraform init..."
terraform init

echo "Running terraform plan..."
terraform plan -out=tfplan

echo ""
echo "Review the plan above. Apply? (y/N)"
read -r confirm
if [ "$confirm" = "y" ] || [ "$confirm" = "Y" ]; then
  terraform apply tfplan
  echo ""
  echo "=== Infrastructure provisioned ==="
  echo "ArgoCD:  http://localhost:30080"
  echo "Grafana: http://localhost:30000 (admin / see terraform.tfvars)"
  echo "Vault:   kubectl port-forward -n vault svc/vault 8200:8200"
else
  echo "Aborted."
  rm -f tfplan
fi
