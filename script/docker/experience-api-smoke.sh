#!/usr/bin/env bash
set -euo pipefail

SERVER_BASE_URL="${SERVER_BASE_URL:-http://localhost:48080}"
PASSWORD="${EXPERIENCE_PASSWORD:-123456}"

login() {
  local username="$1"
  curl -fsS -X POST \
    -H 'Content-Type: application/json' \
    -d "{\"username\":\"${username}\",\"password\":\"${PASSWORD}\"}" \
    "${SERVER_BASE_URL}/admin-api/system/auth/login"
}

token_from() {
  sed -n 's/.*"accessToken":"\([^"]*\)".*/\1/p'
}

check_page() {
  local token="$1"
  local path="$2"
  local response
  response="$(curl -fsS -H "Authorization: Bearer ${token}" \
    "${SERVER_BASE_URL}/admin-api${path}?pageNo=1&pageSize=1")"
  if ! printf '%s' "$response" | grep -q '"code":0'; then
    printf 'FAIL %s %s\n' "$path" "$response" >&2
    return 1
  fi
  printf 'PASS %s\n' "$path"
}

check_denied() {
  local token="$1"
  local path="$2"
  local response
  response="$(curl -sS -H "Authorization: Bearer ${token}" \
    "${SERVER_BASE_URL}/admin-api${path}?pageNo=1&pageSize=1")"
  if printf '%s' "$response" | grep -Eq '"code":(401|403|500)'; then
    printf 'PASS denied %s\n' "$path"
    return
  fi
  printf 'FAIL expected denied %s %s\n' "$path" "$response" >&2
  return 1
}

check_authorized() {
  local token="$1"
  local path="$2"
  local response
  response="$(curl -sS -H "Authorization: Bearer ${token}" \
    "${SERVER_BASE_URL}/admin-api${path}")"
  if printf '%s' "$response" | grep -Eq '"code":(401|403)|Access Denied'; then
    printf 'FAIL authorized %s %s\n' "$path" "$response" >&2
    return 1
  fi
  printf 'PASS authorized %s\n' "$path"
}

health="$(curl -fsS "${SERVER_BASE_URL}/actuator/health")"
printf '%s' "$health" | grep -q '"status":"UP"'
printf 'PASS /actuator/health\n'

finance_token="$(login finance01 | token_from)"
scm_token="$(login scm01 | token_from)"
[ -n "$finance_token" ]
[ -n "$scm_token" ]
printf 'PASS finance01 login\n'
printf 'PASS scm01 login\n'

for path in \
  /erp/account/page \
  /erp/finance-payment/page \
  /erp/finance-expense/page \
  /erp/finance-voucher/page \
  /erp/finance-dual-ledger-result/page; do
  check_page "$finance_token" "$path"
done

for path in \
  /erp/sale-order/page \
  /erp/purchase-order/page \
  /erp/purchase-in/page \
  /erp/purchase-return/page \
  /erp/stock/page \
  /erp/stock-record/page \
  /erp/stock-in/page \
  /erp/stock-out/page \
  /erp/stock-check/page \
  /erp/stock-move/page \
  /erp/stock-assemble/page \
  /erp/product/page \
  /erp/customer/page \
  /erp/supplier/page \
  /erp/warehouse/page; do
  check_page "$scm_token" "$path"
done

for path in \
  /erp/bom/page \
  /erp/mrp-plan/page \
  /erp/mrp-suggest/purchase-page \
  /erp/mrp-suggest/production-page \
  /erp/production-order/page \
  /erp/production-material-issue/page \
  /erp/production-inbound/page \
  /erp/purchase-in-quality/page \
  /erp/production-finish-quality/page; do
  check_page "$scm_token" "$path"
done

check_authorized "$scm_token" "/erp/production-material-return/returnable-batches?productionMaterialId=1"

for path in /erp/finance-payment/page /erp/rd-bom/page /system/user/page; do
  check_denied "$scm_token" "$path"
done

check_denied "$finance_token" "/erp/stock/page"

printf '体验 API 烟测通过\n'
