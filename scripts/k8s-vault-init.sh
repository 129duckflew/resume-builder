#!/usr/bin/env bash
set -euo pipefail

echo "=== Resume Builder: Vault Init ==="

# Ensure Vault is running
if ! kubectl get pods -n vault -l app.kubernetes.io/name=vault &>/dev/null; then
  echo "Error: Vault not found. Run terraform apply first."
  exit 1
fi

# Wait for Vault pod to be ready
echo "Waiting for Vault pod..."
kubectl wait --for=condition=Ready pod -l app.kubernetes.io/name=vault -n vault --timeout=120s

# Enable KV v2 secrets engine (idempotent)
echo "Enabling KV v2 secrets engine..."
kubectl exec -n vault vault-0 -- vault secrets enable -path=secret kv-v2 2>/dev/null || true

# Prompt for secrets
echo ""
echo "Enter secret values (press Enter to keep default):"

read -rp "DB_USER [resume]: " DB_USER
DB_USER="${DB_USER:-resume}"

read -rp "DB_PASS [resume123]: " DB_PASS
DB_PASS="${DB_PASS:-resume123}"

read -rp "JWT_SECRET (leave empty to generate): " JWT_SECRET
if [ -z "$JWT_SECRET" ]; then
  JWT_SECRET=$(openssl rand -hex 32)
  echo "Generated JWT_SECRET: ${JWT_SECRET:0:16}..."
fi

read -rp "MCP_API_KEY (leave empty to generate): " MCP_API_KEY
if [ -z "$MCP_API_KEY" ]; then
  MCP_API_KEY=$(openssl rand -hex 16)
  echo "Generated MCP_API_KEY: $MCP_API_KEY"
fi

# Write secrets to Vault
echo ""
echo "Writing secrets to Vault..."
kubectl exec -n vault vault-0 -- vault kv put secret/resume-builder \
  DB_USER="$DB_USER" \
  DB_PASS="$DB_PASS" \
  JWT_SECRET="$JWT_SECRET" \
  MCP_API_KEY="$MCP_API_KEY"

# Verify
echo ""
echo "Verifying secrets..."
kubectl exec -n vault vault-0 -- vault kv get secret/resume-builder

# Configure K8s auth (if not already done by Terraform)
echo ""
echo "Configuring Kubernetes auth..."
kubectl exec -n vault vault-0 -- vault auth enable kubernetes 2>/dev/null || true
kubectl exec -n vault vault-0 -- sh -c '
  vault write auth/kubernetes/config \
    kubernetes_host="https://kubernetes.default.svc" \
    disable_local_ca_jwt=true
'

# Create policy
kubectl exec -n vault vault-0 -- vault policy write resume-builder - <<'EOF'
path "secret/data/resume-builder" {
  capabilities = ["read"]
}
EOF

# Create role
kubectl exec -n vault vault-0 -- vault write auth/kubernetes/role/resume-builder \
  bound_service_account_names=default \
  bound_service_account_namespaces=resume-builder \
  policies=resume-builder \
  ttl=1h

echo ""
echo "=== Vault Init Complete ==="
echo "Secrets stored at: secret/resume-builder"
echo "K8s auth role: resume-builder"
echo ""
echo "To verify:"
echo "  kubectl exec -n vault vault-0 -- vault kv get secret/resume-builder"
