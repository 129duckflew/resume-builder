#!/bin/bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://resume.local}"

echo "Running smoke tests against $BASE_URL"

echo "1. Frontend responds..."
HTTP_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL")
if [ "$HTTP_CODE" -eq 200 ]; then
  echo "   PASS: HTTP $HTTP_CODE"
else
  echo "   FAIL: HTTP $HTTP_CODE"
  exit 1
fi

echo "2. API responds (public themes endpoint)..."
API_CODE=$(curl -s -o /dev/null -w "%{http_code}" "$BASE_URL/api/themes")
if [ "$API_CODE" -eq 200 ]; then
  echo "   PASS: HTTP $API_CODE"
else
  echo "   FAIL: HTTP $API_CODE"
  exit 1
fi

echo "3. Pods are all Running..."
POD_STATUS=$(kubectl get pods -n resume-builder --no-headers 2>&1 | awk '{print $3}' | sort -u)
if [ "$POD_STATUS" = "Running" ]; then
  echo "   PASS: All pods Running"
else
  echo "   FAIL: Some pods not running"
  kubectl get pods -n resume-builder
  exit 1
fi

echo "All smoke tests passed!"
