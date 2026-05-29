#!/usr/bin/env bash
set -euo pipefail

PASSWORD="${1:-123456}"

for i in $(seq 1 60); do
  if docker exec mysql sh -lc "mysql -uroot -p${PASSWORD} -e 'SELECT 1'" >/tmp/wsl_mysql_prepare.log 2>&1; then
    docker exec mysql sh -lc "mysql -uroot -p${PASSWORD} -e \"CREATE DATABASE IF NOT EXISTS \\\`ruoyi-vue-pro\\\` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;\""
    docker exec mysql sh -lc "mysql -uroot -p${PASSWORD} -e \"SHOW DATABASES LIKE 'ruoyi-vue-pro';\""
    exit 0
  fi
  sleep 1
done

cat /tmp/wsl_mysql_prepare.log 2>/dev/null || true
exit 1
