#!/bin/bash
set -euo pipefail

echo "Building backend image..."
eval $(minikube docker-env)
docker build -t resume-builder-backend:latest ./backend

echo "Building frontend image..."
docker build -t resume-builder-frontend:latest ./frontend

echo "Tagging and pushing to minikube registry..."
docker tag resume-builder-backend:latest localhost:5000/resume-builder-backend:latest
docker tag resume-builder-frontend:latest localhost:5000/resume-builder-frontend:latest
docker push localhost:5000/resume-builder-backend:latest
docker push localhost:5000/resume-builder-frontend:latest

echo "Images built and pushed."
