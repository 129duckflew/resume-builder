#!/usr/bin/env bash
set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(dirname "$SCRIPT_DIR")"
ARGOCD_DIR="$PROJECT_ROOT/infra/argocd"

echo "=== Resume Builder: ArgoCD Bootstrap ==="

# Ensure ArgoCD is running
if ! kubectl get ns argocd &>/dev/null; then
  echo "Error: ArgoCD namespace not found. Run terraform apply first."
  exit 1
fi

# Get GitHub repo URL
REMOTE_URL=$(git -C "$PROJECT_ROOT" remote get-url origin 2>/dev/null || echo "")
if [ -z "$REMOTE_URL" ]; then
  echo "Error: No git remote 'origin' found."
  exit 1
fi

# Extract owner/repo from URL
# Handles: https://github.com/owner/repo.git and git@github.com:owner/repo.git
if [[ "$REMOTE_URL" =~ github\.com[:/]([^/]+)/([^/.]+) ]]; then
  REPO_OWNER="${BASH_REMATCH[1]}"
  echo "Detected GitHub owner: $REPO_OWNER"
else
  echo "Error: Cannot parse GitHub owner from remote URL: $REMOTE_URL"
  echo "Please set GITHUB_REPO_OWNER environment variable."
  read -rp "Enter GitHub owner (username or org): " REPO_OWNER
fi

# Replace placeholder in all ArgoCD manifests
echo "Configuring repoURL with owner: $REPO_OWNER"
find "$ARGOCD_DIR" -name '*.yaml' -exec sed -i '' "s|__GITHUB_REPO_OWNER__|$REPO_OWNER|g" {} +

# Apply app-of-apps
echo "Deploying ArgoCD App of Apps..."
kubectl apply -f "$ARGOCD_DIR/app-of-apps.yaml"

# Wait for ArgoCD to sync
echo "Waiting for ArgoCD to detect applications..."
sleep 5

# Get ArgoCD initial admin password
ARGOCD_POD=$(kubectl get pods -n argocd -l app.kubernetes.io/name=argocd-server -o jsonpath='{.items[0].metadata.name}' 2>/dev/null || echo "")
if [ -n "$ARGOCD_POD" ]; then
  ARGOCD_PASS=$(kubectl -n argocd get secret argocd-initial-admin-secret -o jsonpath="{.data.password}" 2>/dev/null | base64 -d 2>/dev/null || echo "N/A")
  echo ""
  echo "=== ArgoCD Bootstrap Complete ==="
  echo "ArgoCD UI:     http://localhost:30080"
  echo "ArgoCD User:   admin"
  echo "ArgoCD Pass:   $ARGOCD_POD"
  echo ""
  echo "To login via CLI:"
  echo "  argocd login localhost:30080 --username admin --password '$ARGOCD_POD' --insecure"
  echo ""
  echo "Applications will auto-sync from main branch."
else
  echo "Warning: ArgoCD server pod not found yet. Check status with:"
  echo "  kubectl get pods -n argocd"
fi
