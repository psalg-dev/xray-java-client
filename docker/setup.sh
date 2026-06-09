#!/usr/bin/env bash
# Start the JFrog Platform trial stack and wait until Artifactory + Xray are ready.
# Usage: ./setup.sh [license-file]
#
# Steps before running:
#   1. Register at https://jfrog.com/start-free/ to get a trial license
#   2. Pass the license file path as argument, or save it as docker/artifactory.lic

set -euo pipefail

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$SCRIPT_DIR"

LICENSE_FILE="${1:-artifactory.lic}"
if [ ! -f "$LICENSE_FILE" ]; then
  echo "ERROR: License file '$LICENSE_FILE' not found."
  echo "Register at https://jfrog.com/start-free/ to obtain a trial license."
  exit 1
fi

cp "$LICENSE_FILE" ./artifactory.lic

echo "==> Starting JFrog Platform stack..."
docker compose up -d

echo "==> Waiting for Artifactory to be ready (may take 3-5 min on first start)..."
until curl -sf http://localhost:8082/artifactory/api/system/ping > /dev/null 2>&1; do
  printf '.'
  sleep 5
done
echo ""
echo "==> Artifactory is up!"

echo "==> Waiting for Xray to be ready..."
XRAY_READY=0
for i in $(seq 1 60); do
  STATUS=$(curl -su admin:password http://localhost:8082/xray/api/v1/system/ping 2>/dev/null || true)
  if echo "$STATUS" | grep -q '"status":"pong"'; then
    XRAY_READY=1
    break
  fi
  printf '.'
  sleep 5
done
echo ""

if [ "$XRAY_READY" -eq 0 ]; then
  echo "WARNING: Xray did not become ready in time. Check: docker compose logs xray"
else
  echo "==> Xray is up!"
fi

echo ""
echo "Platform ready:"
echo "  Artifactory UI: http://localhost:8082/ui  (admin / password)"
echo "  Xray API:       http://localhost:8082/xray/api/v1"
echo ""
echo "Run integration tests:"
echo "  XRAY_BASE_URL=http://localhost:8082 XRAY_USER=admin XRAY_PASSWORD=password mvn verify -Pintegration"
