#!/bin/bash
set -euo pipefail

echo "Ensuring Colima k3s is running..."
colima status 2>/dev/null || colima start

echo "Waiting for k3s to be ready..."
kubectl wait --for=condition=Ready nodes --all --timeout=120s

echo "Waiting for Traefik ingress controller..."
kubectl wait -n kube-system --for=condition=Available deployment/traefik --timeout=120s

echo "Adding hosts entries..."
sudo sed -i '' '/resume\.local/d' /etc/hosts
echo "127.0.0.1 resume.local grafana.resume.local" | sudo tee -a /etc/hosts

echo "============================================"
echo "Cluster ready. Open in browser:"
echo "  Frontend: http://resume.local"
echo "  Grafana:  http://grafana.resume.local"
echo "============================================"
