#!/bin/bash
set -euo pipefail

KEDA_VERSION="${KEDA_VERSION:-2.16.1}"
HTTP_ADDON_VERSION="${HTTP_ADDON_VERSION:-0.15.0}"

echo "Installing KEDA core ${KEDA_VERSION}..."
# Server-side apply is required for the ScaledJob CRD, whose OpenAPI schema is too
# large to fit in the client-side apply annotation.
kubectl apply --server-side -f "https://github.com/kedacore/keda/releases/download/v${KEDA_VERSION}/keda-${KEDA_VERSION}.yaml"

echo "Installing KEDA HTTP Add-on ${HTTP_ADDON_VERSION}..."
# Server-side apply is also used here for the HTTPScaledObject/InterceptorRoute CRDs, which can
# be large enough to trigger the client-side apply annotation limit.
kubectl apply --server-side -f "https://github.com/kedacore/http-add-on/releases/download/v${HTTP_ADDON_VERSION}/keda-http-add-on-${HTTP_ADDON_VERSION}.yaml"

echo "Waiting for KEDA core to be ready..."
kubectl wait -n keda --for=condition=Available deployment/keda-operator --timeout=120s
kubectl wait -n keda --for=condition=Available deployment/keda-metrics-apiserver --timeout=120s
kubectl wait -n keda --for=condition=Available deployment/keda-admission-webhooks --timeout=120s

echo "Waiting for KEDA HTTP Add-on to be ready..."
kubectl wait -n keda --for=condition=Available deployment/keda-add-ons-http-operator --timeout=120s
kubectl wait -n keda --for=condition=Available deployment/keda-add-ons-http-interceptor --timeout=120s
kubectl wait -n keda --for=condition=Available deployment/keda-add-ons-http-scaler --timeout=120s

echo "KEDA and HTTP Add-on are ready."
