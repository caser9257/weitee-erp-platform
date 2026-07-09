#!/usr/bin/env sh
set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
REPO_ROOT=$(CDPATH= cd -- "$SCRIPT_DIR/../.." && pwd)

MYSQL_CONTAINER="${MYSQL_CONTAINER:-weitee-mysql}"
MYSQL_DATABASE="${MYSQL_DATABASE:-weitee-erp}"
MYSQL_ROOT_PASSWORD="${MYSQL_ROOT_PASSWORD:-123456}"
SQL_PATH="${SQL_PATH:-$REPO_ROOT/production/ruoyi-vue-pro/weitee-erp_backup_20260709.sql}"
BACKUP_DIR="${BACKUP_DIR:-$REPO_ROOT/production/ruoyi-vue-pro/backups}"

if [ "${CONFIRM_RESTORE:-}" != "1" ]; then
  echo "ERROR: restore is blocked by default because the SQL contains DROP TABLE." >&2
  echo "Set CONFIRM_RESTORE=1 after confirming the current database has been backed up." >&2
  exit 2
fi

if [ ! -f "$SQL_PATH" ]; then
  echo "ERROR: database dump not found: $SQL_PATH" >&2
  exit 1
fi

if ! docker ps --format '{{.Names}}' | grep -Fx "$MYSQL_CONTAINER" >/dev/null 2>&1; then
  echo "ERROR: MySQL container is not running: $MYSQL_CONTAINER" >&2
  exit 1
fi

mkdir -p "$BACKUP_DIR"
TIMESTAMP=$(date +%Y%m%d-%H%M%S)
BACKUP_PATH="$BACKUP_DIR/${MYSQL_DATABASE}-before-restore-${TIMESTAMP}.sql"

echo "Backing up current database to: $BACKUP_PATH"
docker exec -e MYSQL_PWD="$MYSQL_ROOT_PASSWORD" "$MYSQL_CONTAINER" \
  mysqldump -uroot --single-transaction --routines --triggers --events "$MYSQL_DATABASE" > "$BACKUP_PATH"

echo "Restoring experience database from: $SQL_PATH"
docker exec -i -e MYSQL_PWD="$MYSQL_ROOT_PASSWORD" "$MYSQL_CONTAINER" \
  mysql -uroot "$MYSQL_DATABASE" < "$SQL_PATH"

echo "Restore completed. Backup kept at: $BACKUP_PATH"
