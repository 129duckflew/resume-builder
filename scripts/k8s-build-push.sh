#!/bin/bash
set -euo pipefail

echo "Building backend image..."
docker build -t resume-builder-backend:latest ./backend

echo "Building frontend image..."
docker build -t resume-builder-frontend:latest ./frontend

echo "Images built. Available to k3s via Colima's shared containerd."
