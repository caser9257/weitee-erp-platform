#!/usr/bin/env bash
set -euo pipefail
for i in $(seq 1 30); do
  if docker exec mysql sh -lc "mysql -uroot -p123456 -e 'SHOW DATABASES LIKE \\\"ruoyi-vue-pro\\\";'" >/tmp/mysql-showdb.log 2>&1; then
    echo SHOWDB_OK
    cat /tmp/mysql-showdb.log
    exit 0
  fi
  sleep 1
done
cat /tmp/mysql-showdb.log 2>/dev/null || true
exit 1
