#!/usr/bin/env bash
set -euo pipefail
for i in $(seq 1 30); do
  if docker ps --format '{{.Names}}' | grep -qx mysql; then
    if docker exec mysql sh -lc "mysql -uroot -pRoot@123456 -e 'SELECT 1'" >/tmp/mysql-probe.log 2>&1; then
      echo READY_WITH_ROOT_AT_123456
      cat /tmp/mysql-probe.log
      exit 0
    fi
    if docker exec mysql sh -lc "mysql -uroot -p123456 -e 'SELECT 1'" >/tmp/mysql-probe.log 2>&1; then
      echo READY_WITH_123456
      cat /tmp/mysql-probe.log
      exit 0
    fi
  fi
  sleep 1
done
cat /tmp/mysql-probe.log 2>/dev/null || true
exit 1
