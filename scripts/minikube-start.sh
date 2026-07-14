#!/bin/bash
set -euo pipefail

echo "Starting minikube..."
minikube start --cpus=4 --memory=8g

echo "Enabling addons..."
minikube addons enable ingress
minikube addons enable metrics-server
minikube addons enable registry

echo "Adding resume.local to /etc/hosts (127.0.0.1 for Docker driver)..."
sudo sed -i '' '/resume.local/d' /etc/hosts
echo "127.0.0.1 resume.local" | sudo tee -a /etc/hosts

echo "============================================"
echo "minikube started. Run the following in a"
echo "separate terminal to access the cluster:"
echo ""
echo "  minikube tunnel"
echo ""
echo "Then open http://resume.local in your browser"
echo "============================================"
