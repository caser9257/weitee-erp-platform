#!/usr/bin/env bash
set -euo pipefail
for i in $(seq 1 30); do
  if docker exec mysql sh -lc "mysql -uroot -pRoot@123456 -e \"ALTER USER 'root'@'%' IDENTIFIED BY '123456'; CREATE DATABASE IF NOT EXISTS \\\`ruoyi-vue-pro\\\` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;\"" >/tmp/mysql-align.log 2>&1; then
    echo ALIGN_OK
    cat /tmp/mysql-align.log
    exit 0
  fi
  sleep 1
done
cat /tmp/mysql-align.log 2>/dev/null || true
exit 1
