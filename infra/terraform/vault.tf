# HashiCorp Vault deployment for secrets management

resource "helm_release" "vault" {
  name             = "vault"
  repository       = "https://helm.releases.hashicorp.com"
  chart            = "vault"
  version          = var.vault_chart_version
  namespace        = "vault"
  create_namespace = true
  wait             = true
  timeout          = 300

  values = [
    <<-EOT
    server:
      dev:
        enabled: true
      resources:
        requests:
          memory: "256Mi"
          cpu: "250m"
        limits:
          memory: "512Mi"
          cpu: "500m"
    injector:
      enabled: true
    EOT
  ]
}
