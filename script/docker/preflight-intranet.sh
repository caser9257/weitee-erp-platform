#!/usr/bin/env sh
set -eu

SCRIPT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
REPO_ROOT=$(CDPATH= cd -- "$SCRIPT_DIR/../.." && pwd)

COMPOSE_PROJECT_NAME="${COMPOSE_PROJECT_NAME:-weitee-system}"
MYSQL_VOLUME="${MYSQL_VOLUME:-${COMPOSE_PROJECT_NAME}_mysql}"
SQL_PATH="${SQL_PATH:-$REPO_ROOT/production/ruoyi-vue-pro/weitee-erp_backup_20260709.sql}"

if ! command -v docker >/dev/null 2>&1; then
  echo "ERROR: docker command not found." >&2
  exit 1
fi

if [ ! -f "$SQL_PATH" ]; then
  echo "ERROR: database dump not found: $SQL_PATH" >&2
  exit 1
fi

if docker volume ls --format '{{.Name}}' | grep -Fx "$MYSQL_VOLUME" >/dev/null 2>&1; then
  echo "ERROR: existing MySQL volume detected: $MYSQL_VOLUME" >&2
  echo "The init SQL will not run automatically on a non-empty MySQL data directory." >&2
  echo "Use restore-experience-db.sh after backing up, or deploy with a fresh volume." >&2
  exit 2
fi

echo "OK: preflight passed. MySQL init SQL should run on first startup."
echo "SQL: $SQL_PATH"
