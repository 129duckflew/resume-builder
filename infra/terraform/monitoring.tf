# kube-prometheus-stack (Prometheus + Grafana) deployment

resource "helm_release" "kube_prometheus_stack" {
  name             = "kube-prometheus-stack"
  repository       = "https://prometheus-community.github.io/helm-charts"
  chart            = "kube-prometheus-stack"
  version          = "69.3.1"
  namespace        = "monitoring"
  create_namespace = true
  wait             = true
  timeout          = 600

  values = [
    <<-EOT
    grafana:
      adminPassword: "${var.grafana_admin_password}"
      service:
        type: NodePort
        port: 3000
        nodePort: 30000

    prometheus:
      prometheusSpec:
        retention: "${var.prometheus_retention}"
        scrapeInterval: "15s"
        evaluationInterval: "15s"

    # Disable scrapers for components not available on Colima k3s
    kubeEtcd:
      enabled: false
    kubeScheduler:
      enabled: false
    kubeControllerManager:
      enabled: false
    kubelet:
      enabled: false

    defaultRules:
      rules:
        etcd: false
        kubeScheduler: false
        kubeControllerManager: false
        kubelet: false
    EOT
  ]
}
