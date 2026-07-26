# HashiCorp Vault deployment for secrets management
# Uses standalone (non-dev) mode with file storage on a PVC so auth config, policies,
# roles and secrets survive Colima restarts. A sidecar auto-unseals on pod start using
# a single unseal key stored in Kubernetes secret `vault-unseal-key`.

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
        enabled: false
      standalone:
        enabled: true
        config: |
          ui = true
          listener "tcp" {
            tls_disable = 1
            address     = "[::]:8200"
          }
          storage "file" {
            path = "/vault/data"
          }
      dataStorage:
        enabled: true
        size: 1Gi
      resources:
        requests:
          memory: "256Mi"
          cpu: "250m"
        limits:
          memory: "512Mi"
          cpu: "500m"
      extraEnvironmentVars:
        VAULT_ADDR: "http://127.0.0.1:8200"
      volumes:
        - name: vault-unseal-key
          secret:
            secretName: vault-unseal-key
            optional: true
      volumeMounts:
        - name: vault-unseal-key
          mountPath: /vault/unseal
          readOnly: true
      extraContainers:
        - name: vault-unsealer
          image: hashicorp/vault:1.16.1
          command:
            - "/bin/sh"
            - "-ec"
            - |
              while ! vault status >/dev/null 2>&1; do
                sleep 3
              done
              if vault status 2>&1 | grep -Eq '^Sealed  *true'; then
                if [ -f /vault/unseal/key ]; then
                  vault operator unseal "$(cat /vault/unseal/key)"
                fi
              fi
              while true; do sleep 3600; done
          env:
            - name: VAULT_ADDR
              value: "http://127.0.0.1:8200"
    injector:
      enabled: true
    EOT
  ]
}
