#!/usr/bin/env bash
set -euo pipefail

for i in $(seq 1 60); do
  if docker ps --format '{{.Names}}' | grep -qx mysql; then
    if docker exec mysql sh -lc "mysql -uroot -p123456 -e 'SELECT 1'" >/tmp/wsl_mysql_probe.log 2>&1; then
      echo "MYSQL_READY=password_123456"
      docker exec mysql sh -lc "mysql -uroot -p123456 -e 'SHOW DATABASES'"
      exit 0
    fi
    if docker exec mysql sh -lc "mysql -uroot -pRoot@123456 -e 'SELECT 1'" >/tmp/wsl_mysql_probe.log 2>&1; then
      echo "MYSQL_READY=password_Root@123456"
      docker exec mysql sh -lc "mysql -uroot -pRoot@123456 -e 'SHOW DATABASES'"
      exit 0
    fi
  fi
  sleep 1
done

cat /tmp/wsl_mysql_probe.log 2>/dev/null || true
exit 1
