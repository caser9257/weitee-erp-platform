#!/usr/bin/env bash
set -euo pipefail

COMPOSE_FILE="${COMPOSE_FILE:-script/docker/docker-compose.yml}"
SERVER_BASE_URL="${SERVER_BASE_URL:-http://localhost:48080}"
ADMIN_BASE_URL="${ADMIN_BASE_URL:-http://localhost:8080}"
LOGIN_USERNAME="${LOGIN_USERNAME:-admin}"
LOGIN_PASSWORD="${LOGIN_PASSWORD:-admin123}"
SKIP_CLEANUP="${SKIP_CLEANUP:-false}"

SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
REPO_ROOT="$(cd "$SCRIPT_DIR/../.." && pwd)"
COMPOSE_PATH="$REPO_ROOT/$COMPOSE_FILE"
PASSED=0
FAILED=0

check() {
  local name="$1"; shift
  if "$@"; then
    printf '  PASS: %s\n' "$name"
    PASSED=$((PASSED + 1))
  else
    printf '  FAIL: %s\n' "$name" >&2
    FAILED=$((FAILED + 1))
  fi
}

service_id() {
  docker compose -f "$COMPOSE_PATH" ps -q "$1"
}

wait_health() {
  local id status
  id="$(service_id "$1")"
  [ -n "$id" ] || return 1
  for _ in $(seq 1 60); do
    status="$(docker inspect --format '{{.State.Health.Status}}' "$id" 2>/dev/null || true)"
    [ "$status" = "healthy" ] && return 0
    sleep 2
  done
  return 1
}

api_get() {
  local path="$1" token="$2" response code
  response="$(curl -fsS -H "Authorization: Bearer $token" "$SERVER_BASE_URL$path")" || return 1
  code="$(printf '%s' "$response" | sed -n 's/.*"code":\([0-9]*\).*/\1/p')"
  [ "$code" = "0" ]
}

echo '=== Docker Supply Chain and Finance Smoke Test ==='
command -v docker >/dev/null
command -v curl >/dev/null

if [ "$SKIP_CLEANUP" != 'true' ]; then
  check 'Remove existing compose volumes' docker compose -f "$COMPOSE_PATH" down -v
fi
check 'Build and start compose services' docker compose -f "$COMPOSE_PATH" up -d --build
check 'MySQL healthy' wait_health mysql
check 'Redis healthy' wait_health redis
check 'Server health UP' bash -c "for i in \$(seq 1 60); do curl -fsS '$SERVER_BASE_URL/actuator/health' | grep -q '\"status\":\"UP\"' && exit 0; sleep 3; done; exit 1"
check 'Admin page accessible' curl -fsS "$ADMIN_BASE_URL"
check 'Server logs have no startup blocker' bash -c "! docker compose -f '$COMPOSE_PATH' logs server --no-color | grep -E 'UnsupportedClassVersionError|Flyway.*(failed|exception)|SQL.*initialization.*(failed|exception)'"

LOGIN_BODY="{\"username\":\"$LOGIN_USERNAME\",\"password\":\"$LOGIN_PASSWORD\"}"
LOGIN_RESPONSE="$(curl -fsS -X POST -H 'Content-Type: application/json' -d "$LOGIN_BODY" "$SERVER_BASE_URL/admin-api/system/auth/login" || true)"
TOKEN="$(printf '%s' "$LOGIN_RESPONSE" | sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p')"
if [ -n "$TOKEN" ]; then
  printf '  PASS: Login and get access token\n'
  PASSED=$((PASSED + 1))
else
  printf '  FAIL: Login and get access token\n' >&2
  FAILED=$((FAILED + 1))
fi

PAGE_APIS=(
  '/admin-api/erp/sale-order/page?pageNo=1&pageSize=1'
  '/admin-api/erp/purchase-order/page?pageNo=1&pageSize=1'
  '/admin-api/erp/purchase-in/page?pageNo=1&pageSize=1'
  '/admin-api/erp/purchase-return/page?pageNo=1&pageSize=1'
  '/admin-api/erp/stock-in/page?pageNo=1&pageSize=1'
  '/admin-api/erp/stock-out/page?pageNo=1&pageSize=1'
  '/admin-api/erp/stock-check/page?pageNo=1&pageSize=1'
  '/admin-api/erp/stock/page?pageNo=1&pageSize=1'
  '/admin-api/erp/stock-move/page?pageNo=1&pageSize=1'
  '/admin-api/erp/finance-payment/page?pageNo=1&pageSize=1'
  '/admin-api/erp/finance-expense/page?pageNo=1&pageSize=1'
  '/admin-api/erp/finance-voucher/page?pageNo=1&pageSize=1'
  '/admin-api/erp/finance-dual-ledger-result/page?pageNo=1&pageSize=1'
  '/admin-api/erp/product/page?pageNo=1&pageSize=1'
  '/admin-api/erp/customer/page?pageNo=1&pageSize=1'
  '/admin-api/erp/supplier/page?pageNo=1&pageSize=1'
  '/admin-api/erp/warehouse/page?pageNo=1&pageSize=1'
)

for path in "${PAGE_APIS[@]}"; do
  check "GET ${path%%\?*}" api_get "$path" "$TOKEN"
done

echo "=== Result: $PASSED passed, $FAILED failed ==="
[ "$FAILED" -eq 0 ]
