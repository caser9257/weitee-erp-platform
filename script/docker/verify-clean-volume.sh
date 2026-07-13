#!/usr/bin/env bash
set -euo pipefail

COMPOSE_PROJECT_NAME="${COMPOSE_PROJECT_NAME:-weitee-system}"
COMPOSE_FILE="${COMPOSE_FILE:-script/docker/docker-compose.yml}"
SERVER_BASE_URL="${SERVER_BASE_URL:-http://localhost:48080}"

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
COMPOSE_PATH="$REPO_ROOT/$COMPOSE_FILE"

require_cmd() { if ! command -v "$1" &>/dev/null; then echo "ERROR: $1 not found"; exit 1; fi }
require_cmd docker

echo "========================================"
echo " Docker Clean Volume Verification"
echo "========================================"

echo ""
echo "[1/5] Cleaning up existing containers and volumes..."
if docker compose ls 2>/dev/null | grep -q "$COMPOSE_PROJECT_NAME"; then
  docker compose -f "$COMPOSE_PATH" down -v
fi
echo "  OK"

echo ""
echo "[2/5] Building and starting services..."
docker compose -f "$COMPOSE_PATH" up -d --build
echo "  OK"

echo ""
echo "[3/5] Waiting for MySQL health..."
for i in $(seq 1 60); do
  status=$(docker inspect --format '{{.State.Health.Status}}' "${COMPOSE_PROJECT_NAME}-mysql-1" 2>/dev/null || true)
  [ "$status" = "healthy" ] && { echo "  OK"; break; }
  sleep 2
done
if [ "$status" != "healthy" ]; then echo "ERROR: MySQL not healthy"; exit 1; fi

echo ""
echo "[3b/5] Waiting for Redis health..."
for i in $(seq 1 30); do
  status=$(docker inspect --format '{{.State.Health.Status}}' "${COMPOSE_PROJECT_NAME}-redis-1" 2>/dev/null || true)
  [ "$status" = "healthy" ] && { echo "  OK"; break; }
  sleep 2
done
if [ "$status" != "healthy" ]; then echo "ERROR: Redis not healthy"; exit 1; fi

echo ""
echo "[3c/5] Waiting for server health..."
for i in $(seq 1 60); do
  resp=$(curl -s -o /dev/null -w "%{http_code}" "$SERVER_BASE_URL/actuator/health" 2>/dev/null || true)
  [ "$resp" = "200" ] && { echo "  OK"; break; }
  sleep 3
done
if [ "$resp" != "200" ]; then echo "ERROR: Server not healthy"; exit 1; fi

echo ""
echo "[4/5] Verifying core page APIs..."
CORE_PAGES=(
  "/admin-api/erp/sale-order/page"
  "/admin-api/erp/purchase-order/page"
  "/admin-api/erp/stock-in/page"
  "/admin-api/erp/stock-out/page"
  "/admin-api/erp/stock-check/page"
  "/admin-api/erp/finance-payment/page"
  "/admin-api/erp/finance-expense/page"
  "/admin-api/erp/finance-voucher/page"
)
FAILED=0
for page in "${CORE_PAGES[@]}"; do
  resp=$(curl -s "$SERVER_BASE_URL$page?pageNo=1&pageSize=1" 2>/dev/null || echo '{"code":-1}')
  code=$(echo "$resp" | grep -o '"code":[0-9]*' | head -1 | cut -d: -f2)
  if [ "$code" = "0" ]; then
    echo "  OK: $page"
  else
    echo "  FAIL: $page (code=$code)"
    FAILED=$((FAILED+1))
  fi
done

echo ""
echo "[5/5] Results..."
if [ "$FAILED" -eq 0 ]; then
  echo "========================================"
  echo " CLEAN VOLUME VERIFICATION PASSED"
  echo " All ${#CORE_PAGES[@]} pages accessible."
  echo "========================================"
else
  echo "========================================"
  echo " CLEAN VOLUME VERIFICATION FAILED"
  echo " $FAILED / ${#CORE_PAGES[@]} checks failed."
  echo "========================================"
  exit 1
fi