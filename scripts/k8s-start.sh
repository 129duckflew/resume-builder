#!/bin/bash
set -euo pipefail

echo "Ensuring Colima k3s is running..."
colima status 2>/dev/null || colima start

echo "Waiting for k3s to be ready..."
kubectl wait --for=condition=Ready nodes --all --timeout=120s

echo "Waiting for Traefik ingress controller..."
kubectl wait -n kube-system --for=condition=Available deployment/traefik --timeout=120s

echo "Waiting for lb-port-forwarder DaemonSet..."
kubectl wait -n resume-builder --for=condition=Ready pod -l app=lb-port-forwarder --timeout=120s 2>/dev/null || true

echo "Killing old port-forwards..."
sudo pkill -f "kubectl port-forward.*svc/traefik" 2>/dev/null || true

echo "Starting port forwarding to Traefik..."
sudo nohup kubectl port-forward -n kube-system svc/traefik 80:80 443:443 > /tmp/k8s-port-forward.log 2>&1 &
PF_PID=$!
sleep 1
if kill -0 $PF_PID 2>/dev/null; then
  echo "  port-forward PID: $PF_PID (enables http://resume.local)"
else
  echo "  WARNING: port-forward failed. Check /tmp/k8s-port-forward.log"
fi

echo "Adding hosts entries..."
sudo sed -i '' '/resume\.local/d' /etc/hosts
echo "127.0.0.1 resume.local grafana.resume.local" | sudo tee -a /etc/hosts

echo "============================================"
echo "Cluster ready. Open in browser:"
echo "  Frontend: http://resume.local"
echo "  Grafana:  http://grafana.resume.local"
echo "============================================"
