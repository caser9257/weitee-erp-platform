#!/usr/bin/env bash
set -euo pipefail

PASSWORD="${1:?password required}"
DATABASE="${2:?database required}"
SQL_FILE="${3:?sql file required}"

if [ ! -f "$SQL_FILE" ]; then
  echo "SQL file not found: $SQL_FILE" >&2
  exit 1
fi

for i in $(seq 1 60); do
  if docker exec mysql sh -lc "mysql -uroot -p${PASSWORD} ${DATABASE} -e 'SELECT 1'" >/tmp/wsl_mysql_exec_file.log 2>&1; then
    docker exec -i mysql sh -lc "mysql -uroot -p${PASSWORD} ${DATABASE}" < "$SQL_FILE"
    exit 0
  fi
  sleep 1
done

cat /tmp/wsl_mysql_exec_file.log 2>/dev/null || true
exit 1
