# Post-deployment Vault configuration: enable Kubernetes auth, create policy and role

resource "null_resource" "vault_k8s_auth" {
  depends_on = [helm_release.vault]

  provisioner "local-exec" {
    command = <<-EOT
      # Wait for Vault pod to be ready
      kubectl wait --for=condition=Ready pod -l app.kubernetes.io/name=vault -n vault --timeout=120s
      
      # Enable K8s auth method
      kubectl exec -n vault vault-0 -- vault auth enable kubernetes 2>/dev/null || true
      
      # Configure K8s auth (auto-detect CA from service account)
      kubectl exec -n vault vault-0 -- sh -c '
        vault write auth/kubernetes/config \
          kubernetes_host="https://kubernetes.default.svc"
      '
      
      # Create policy
      kubectl exec -n vault vault-0 -- vault policy write resume-builder - <<EOF
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
    EOT
  }
}
