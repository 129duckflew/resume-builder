# ArgoCD deployment for GitOps workflow

resource "helm_release" "argocd" {
  name             = "argocd"
  repository       = "https://argoproj.github.io/argo-helm"
  chart            = "argo-cd"
  version          = var.argocd_chart_version
  namespace        = "argocd"
  create_namespace = true
  wait             = true
  timeout          = 600

  values = [
    <<-EOT
    server:
      service:
        type: NodePort
        nodePortHttp: 30080
        nodePortHttps: 30443
      extraArgs:
        - --insecure
    EOT
  ]
}
