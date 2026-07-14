#!/bin/bash
set -euo pipefail

echo "Building backend image..."
eval $(minikube docker-env)
docker build -t resume-builder-backend:latest ./backend

echo "Building frontend image..."
docker build -t resume-builder-frontend:latest ./frontend

echo "Setting up registry port-forward..."
pkill -f "kubectl port-forward.*5000" 2>/dev/null || true
kubectl port-forward -n kube-system service/registry 5000:80 &
sleep 3

echo "Tagging and pushing to minikube registry..."
docker tag resume-builder-backend:latest localhost:5000/resume-builder-backend:latest
docker tag resume-builder-frontend:latest localhost:5000/resume-builder-frontend:latest
docker push localhost:5000/resume-builder-backend:latest
docker push localhost:5000/resume-builder-frontend:latest

echo "Images built and pushed."
