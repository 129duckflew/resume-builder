#!/bin/bash
set -euo pipefail

echo "Starting minikube..."
minikube start --cpus=4 --memory=8g

echo "Enabling addons..."
minikube addons enable ingress
minikube addons enable metrics-server
minikube addons enable registry

echo "Adding resume.local to /etc/hosts..."
echo "$(minikube ip) resume.local" | sudo tee -a /etc/hosts

echo "minikube ready at http://resume.local"
