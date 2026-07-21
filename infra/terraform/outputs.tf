# Infrastructure outputs

output "argocd_server_url" {
  description = "ArgoCD server NodePort URL"
  value       = "http://localhost:30080"
}

output "grafana_url" {
  description = "Grafana NodePort URL"
  value       = "http://localhost:30000"
}

output "vault_address" {
  description = "Vault service address within the cluster"
  value       = "http://vault.vault.svc.cluster.local:8200"
}
