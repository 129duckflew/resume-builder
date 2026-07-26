#!/usr/bin/env bash
set -euo pipefail

echo "=== Resume Builder: Vault Init ==="

# Ensure Vault is running
if ! kubectl get pods -n vault -l app.kubernetes.io/name=vault &>/dev/null; then
  echo "Error: Vault not found. Run terraform apply first."
  exit 1
fi

# Wait for Vault pod to be scheduled and Vault to be responsive
echo "Waiting for Vault pod..."
kubectl wait --for=condition=PodScheduled pod -l app.kubernetes.io/name=vault -n vault --timeout=120s

echo "Waiting for Vault to become responsive..."
VAULT_STATUS=""
for i in $(seq 1 30); do
  if VAULT_STATUS=$(kubectl exec -n vault vault-0 -- vault status 2>&1); then
    break
  fi
  if echo "$VAULT_STATUS" | grep -qE 'Initialized|Sealed'; then
    # vault status returned valid output (e.g. sealed/not init) — exit code != 0 is OK
    break
  fi
  echo "  ($i/30) waiting..."
  sleep 5
done

echo ""
echo "Checking Vault status..."
echo "$VAULT_STATUS"

# --- Handle initialization (first boot with empty PVC) ---
if echo "$VAULT_STATUS" | grep -q 'Initialized *false'; then
  echo ""
  echo "Vault is not initialized. Running 'vault operator init'..."
  kubectl exec -n vault vault-0 -- vault operator init \
    -key-shares=1 -key-threshold=1 2>&1 | tee /tmp/vault-init.txt

  UNSEAL_KEY=$(grep 'Unseal Key 1:' /tmp/vault-init.txt | awk '{print $NF}')
  ROOT_TOKEN=$(grep 'Initial Root Token:' /tmp/vault-init.txt | awk '{print $NF}')

  if [ -z "$UNSEAL_KEY" ] || [ -z "$ROOT_TOKEN" ]; then
    echo "ERROR: Failed to extract unseal key and root token from init output"
    exit 1
  fi

  echo ""
  echo "Storing unseal key and root token in Kubernetes secret..."
  kubectl create secret generic vault-unseal-key -n vault \
    --from-literal=key="$UNSEAL_KEY" \
    --from-literal=root-token="$ROOT_TOKEN" \
    --dry-run=client -o yaml | kubectl apply -f -

  rm -f /tmp/vault-init.txt
  echo "Secret vault-unseal-key created/updated."

  echo "Unsealing Vault..."
  kubectl exec -n vault vault-0 -- vault operator unseal "$UNSEAL_KEY"

# --- Handle sealed state (restart after Colima reboot) ---
elif echo "$VAULT_STATUS" | grep -q 'Sealed *true'; then
  echo ""
  echo "Vault is sealed. Attempting auto-unseal..."
  UNSEAL_KEY=$(kubectl get secret vault-unseal-key -n vault -o jsonpath='{.data.key}' 2>/dev/null | base64 -d) || {
    echo "ERROR: Cannot read unseal key from secret vault-unseal-key."
    echo "If this is a fresh deploy, the sidecar may not have unsealed yet. Wait and retry."
    exit 1
  }
  kubectl exec -n vault vault-0 -- vault operator unseal "$UNSEAL_KEY"
  echo "Vault unsealed."
fi

# --- Login ---
echo ""
ROOT_TOKEN=$(kubectl get secret vault-unseal-key -n vault -o jsonpath='{.data.root-token}' | base64 -d)
echo "Logging in with root token..."
kubectl exec -n vault vault-0 -- vault login "$ROOT_TOKEN" 2>/dev/null

# --- Enable KV v2 secrets engine ---
echo ""
echo "Enabling KV v2 secrets engine at secret/..."
kubectl exec -n vault vault-0 -- vault secrets enable -path=secret kv-v2 2>/dev/null || true

# --- Prompt for secrets ---
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

# --- Write secrets to Vault ---
echo ""
echo "Writing secrets to Vault..."
kubectl exec -n vault vault-0 -- vault kv put secret/resume-builder \
  DB_USER="$DB_USER" \
  DB_PASS="$DB_PASS" \
  JWT_SECRET="$JWT_SECRET" \
  MCP_API_KEY="$MCP_API_KEY"

# --- Verify secrets ---
echo ""
echo "Verifying secrets..."
kubectl exec -n vault vault-0 -- vault kv get secret/resume-builder

# --- Configure Kubernetes auth ---
echo ""
echo "Configuring Kubernetes auth..."
kubectl exec -n vault vault-0 -- vault auth enable kubernetes 2>/dev/null || true
kubectl exec -n vault vault-0 -- sh -c '
  vault write auth/kubernetes/config \
    kubernetes_host="https://kubernetes.default.svc"
'

# --- Create policy ---
kubectl exec -n vault vault-0 -- vault policy write resume-builder - <<'EOF'
path "secret/data/resume-builder" {
  capabilities = ["read"]
}
EOF

# --- Create role ---
kubectl exec -n vault vault-0 -- vault write auth/kubernetes/role/resume-builder \
  bound_service_account_names=default \
  bound_service_account_namespaces=resume-builder \
  policies=resume-builder \
  ttl=1h

echo ""
echo "=== Vault Init Complete ==="
echo "Secrets stored at: secret/resume-builder"
echo "K8s auth role:    resume-builder"
echo "Unseal key:       k8s secret vault-unseal-key"
echo ""
echo "On Colima restart, the vault-unsealer sidecar will auto-unseal — no manual steps needed."
