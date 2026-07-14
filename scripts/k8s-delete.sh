#!/bin/bash
set -euo pipefail

echo "Deleting all resources in resume-builder namespace..."
kubectl delete namespace resume-builder --ignore-not-found

echo "Cleanup complete."
