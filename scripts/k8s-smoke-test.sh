#!/bin/bash
set -euo pipefail

BASE_URL="${BASE_URL:-http://resume.local}"
# Pin DNS resolution to localhost to avoid the 5s mDNS timeout for *.local domains on macOS.
CURL_RESOLVE="${CURL_RESOLVE:- --resolve resume.local:80:127.0.0.1}"
COLD_START_TIMEOUT="${COLD_START_TIMEOUT:-90}"

echo "Running smoke tests against $BASE_URL"

scale_to_zero() {
  echo "Scaling deployments to zero for scale-from-zero test..."
  kubectl scale deployment resume-frontend resume-backend --replicas=0 -n resume-builder >/dev/null
  # Wait until no frontend/backend pods remain.
  for i in $(seq 1 30); do
    local count
    count=$(kubectl get pods -n resume-builder -l 'app in (resume-frontend, resume-backend)' --no-headers 2>/dev/null | wc -l | tr -d ' ')
    if [ "$count" -eq 0 ]; then
      echo "Deployments scaled to zero."
      return 0
    fi
    echo "Waiting for scale-to-zero... ($i/30)"
    sleep 2
  done
  echo "FAIL: deployments did not scale to zero in time"
  exit 1
}

curl_with_time() {
  local url=$1
  local max_time=${2:-$COLD_START_TIMEOUT}
  curl -s -o /dev/null -w "%{http_code} %{time_total}" --max-time "$max_time" $CURL_RESOLVE "$url"
}

scale_to_zero

echo "1. Frontend cold start (scale from zero)..."
FRONTEND_RESULT=$(curl_with_time "$BASE_URL")
echo "   Result: $FRONTEND_RESULT"
FRONTEND_CODE=$(echo "$FRONTEND_RESULT" | awk '{print $1}')
if [ "$FRONTEND_CODE" -eq 200 ]; then
  echo "   PASS: Frontend responded with HTTP $FRONTEND_CODE"
else
  echo "   FAIL: Frontend returned HTTP $FRONTEND_CODE"
  kubectl get pods -n resume-builder
  exit 1
fi

echo "2. API cold start (scale from zero)..."
API_RESULT=$(curl_with_time "$BASE_URL/api/themes")
echo "   Result: $API_RESULT"
API_CODE=$(echo "$API_RESULT" | awk '{print $1}')
if [ "$API_CODE" -eq 200 ]; then
  echo "   PASS: API responded with HTTP $API_CODE"
else
  echo "   FAIL: API returned HTTP $API_CODE"
  kubectl get pods -n resume-builder
  exit 1
fi

echo "3. Hot frontend request..."
HOT_FRONTEND_RESULT=$(curl_with_time "$BASE_URL" 30)
echo "   Result: $HOT_FRONTEND_RESULT"
HOT_FRONTEND_CODE=$(echo "$HOT_FRONTEND_RESULT" | awk '{print $1}')
if [ "$HOT_FRONTEND_CODE" -eq 200 ]; then
  echo "   PASS: Hot frontend request succeeded"
else
  echo "   FAIL: Hot frontend request returned HTTP $HOT_FRONTEND_CODE"
  exit 1
fi

echo "4. Hot API request..."
HOT_API_RESULT=$(curl_with_time "$BASE_URL/api/themes" 30)
echo "   Result: $HOT_API_RESULT"
HOT_API_CODE=$(echo "$HOT_API_RESULT" | awk '{print $1}')
if [ "$HOT_API_CODE" -eq 200 ]; then
  echo "   PASS: Hot API request succeeded"
else
  echo "   FAIL: Hot API request returned HTTP $HOT_API_CODE"
  exit 1
fi

echo "5. Pods are all Running..."
POD_STATUS=$(kubectl get pods -n resume-builder --no-headers 2>&1 | awk '{print $3}' | sort -u)
if [ "$POD_STATUS" = "Running" ]; then
  echo "   PASS: All pods Running"
else
  echo "   FAIL: Some pods not running"
  kubectl get pods -n resume-builder
  exit 1
fi

echo "All smoke tests passed!"
