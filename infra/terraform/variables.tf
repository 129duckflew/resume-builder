# Infrastructure variables for resume-builder deployment

variable "kubeconfig_path" {
  description = "Path to kubeconfig file"
  type        = string
  default     = "~/.kube/config"
}

variable "kubeconfig_context" {
  description = "Kubernetes context to use"
  type        = string
  default     = "colima"
}

variable "argocd_chart_version" {
  description = "ArgoCD Helm chart version"
  type        = string
  default     = "10.1.4"
}

variable "keda_chart_version" {
  description = "KEDA Helm chart version"
  type        = string
  default     = "2.16.1"
}

variable "vault_chart_version" {
  description = "Vault Helm chart version"
  type        = string
  default     = "0.28.0"
}

variable "grafana_admin_password" {
  description = "Admin password for Grafana"
  type        = string
  sensitive   = true
  default     = "admin"
}

variable "prometheus_retention" {
  description = "Prometheus data retention duration"
  type        = string
  default     = "15d"
}
