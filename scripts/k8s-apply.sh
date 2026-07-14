#!/bin/bash
set -euo pipefail

echo "Creating namespace..."
kubectl apply -f k8s/00-namespace/

echo "Applying config..."
kubectl apply -f k8s/01-config/ -n resume-builder

echo "Applying database..."
kubectl apply -f k8s/03-database/ -n resume-builder

echo "Applying backend..."
kubectl apply -f k8s/04-backend/ -n resume-builder

echo "Applying frontend..."
kubectl apply -f k8s/05-frontend/ -n resume-builder

echo "Applying ingress..."
kubectl apply -f k8s/06-ingress/ -n resume-builder

echo "Applying observability..."
kubectl apply -f k8s/07-observability/ -n resume-builder

echo "Waiting for rollouts..."
kubectl rollout status deployment/resume-backend -n resume-builder --timeout=120s
kubectl rollout status deployment/resume-frontend -n resume-builder --timeout=120s

echo "All resources deployed successfully!"
