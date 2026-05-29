#!/usr/bin/env bash
set -euo pipefail

TOKEN="${1:?token required}"
curl -s -H "Authorization: Bearer ${TOKEN}" \
  "http://127.0.0.1:48080/admin-api/system/auth/get-permission-info"
