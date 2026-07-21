# KEDA (Kubernetes Event-Driven Autoscaling) deployment

resource "helm_release" "keda" {
  name             = "keda"
  repository       = "https://kedacore.github.io/charts"
  chart            = "keda"
  version          = var.keda_chart_version
  namespace        = "keda"
  create_namespace = true
  wait             = true
  timeout          = 300
}

resource "helm_release" "keda_http_addon" {
  name             = "keda-add-ons-http"
  repository       = "https://kedacore.github.io/charts"
  chart            = "keda-add-ons-http"
  version          = "0.15.0"
  namespace        = "keda"
  create_namespace = false
  wait             = true
  timeout          = 300
  depends_on       = [helm_release.keda]
}
