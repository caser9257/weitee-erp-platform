#!/usr/bin/env bash
set -euo pipefail

MYSQL_PASSWORD="${1:-123456}"
WORKSPACE_TMP="/mnt/d/ruoyi-vue-pro/tmp"
MYSQL_CONTAINER="mysql"
OFFICIAL_DB="ruoyi-vue-pro"
RESTORE_DB="ruoyi_vue_pro_restore"
SQL_LIST_PATH="${WORKSPACE_TMP}/sql_24_95_paths.txt"
TIMESTAMP="$(date +%Y%m%d-%H%M%S)"

BACKUP_OFFICIAL_PATH="${WORKSPACE_TMP}/official-before-full-restore-${TIMESTAMP}.sql"
PATCHED_RESTORE_PATH="${WORKSPACE_TMP}/restore-after-patch-${TIMESTAMP}.sql"

mysql_exec() {
  docker exec "${MYSQL_CONTAINER}" sh -lc "mysql -uroot -p${MYSQL_PASSWORD} --default-character-set=utf8mb4 \"$1\""
}

wait_mysql() {
  for _ in $(seq 1 60); do
    if docker exec "${MYSQL_CONTAINER}" sh -lc "mysql -uroot -p${MYSQL_PASSWORD} -e 'SELECT 1'" >/dev/null 2>&1; then
      return 0
    fi
    sleep 1
  done
  return 1
}

if [[ ! -f "${SQL_LIST_PATH}" ]]; then
  echo "SQL list not found: ${SQL_LIST_PATH}" >&2
  exit 1
fi

if ! wait_mysql; then
  echo "MySQL container is not ready" >&2
  exit 1
fi

echo "[1/4] Backup official database -> ${BACKUP_OFFICIAL_PATH}"
docker exec "${MYSQL_CONTAINER}" sh -lc \
  "mysqldump -uroot -p${MYSQL_PASSWORD} --set-gtid-purged=OFF --single-transaction --routines --events \"${OFFICIAL_DB}\"" \
  > "${BACKUP_OFFICIAL_PATH}"

echo "[2/4] Replay sql chain to restore database"
while IFS= read -r file; do
  [[ -z "${file}" ]] && continue
  [[ "${file}" != *.sql ]] && continue
  [[ ! -f "${file}" ]] && continue
  echo "  -> $(basename "${file}")"
  sed \
    -e 's/USE `ruoyi-vue-pro`;/USE `ruoyi_vue_pro_restore`;/g' \
    -e 's/USE ruoyi-vue-pro;/USE ruoyi_vue_pro_restore;/g' \
    "${file}" \
    | docker exec -i "${MYSQL_CONTAINER}" sh -lc \
        "mysql -uroot -p${MYSQL_PASSWORD} --default-character-set=utf8mb4 \"${RESTORE_DB}\""
done < "${SQL_LIST_PATH}"

echo "[3/4] Dump patched restore database -> ${PATCHED_RESTORE_PATH}"
docker exec "${MYSQL_CONTAINER}" sh -lc \
  "mysqldump -uroot -p${MYSQL_PASSWORD} --set-gtid-purged=OFF --single-transaction --routines --events \"${RESTORE_DB}\"" \
  > "${PATCHED_RESTORE_PATH}"

echo "[4/4] Replace official database with patched restore database"
docker exec "${MYSQL_CONTAINER}" sh -lc \
  "mysql -uroot -p${MYSQL_PASSWORD} -e \"DROP DATABASE IF EXISTS \\\`${OFFICIAL_DB}\\\`; CREATE DATABASE \\\`${OFFICIAL_DB}\\\` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;\""
docker exec -i "${MYSQL_CONTAINER}" sh -lc \
  "mysql -uroot -p${MYSQL_PASSWORD} --default-character-set=utf8mb4 --database=\"${OFFICIAL_DB}\"" \
  < "${PATCHED_RESTORE_PATH}"

echo "[verify] key counts and superadmin presence"
docker exec "${MYSQL_CONTAINER}" sh -lc \
  "mysql -uroot -p${MYSQL_PASSWORD} -N -e \
  \"SELECT 'official_users' AS label, COUNT(*) AS value FROM \\\`${OFFICIAL_DB}\\\`.system_users UNION ALL
    SELECT 'official_role' AS label, COUNT(*) AS value FROM \\\`${OFFICIAL_DB}\\\`.system_role UNION ALL
    SELECT 'official_menu' AS label, COUNT(*) AS value FROM \\\`${OFFICIAL_DB}\\\`.system_menu UNION ALL
    SELECT 'official_role_menu' AS label, COUNT(*) AS value FROM \\\`${OFFICIAL_DB}\\\`.system_role_menu UNION ALL
    SELECT 'has_superadmin' AS label, COUNT(*) AS value FROM \\\`${OFFICIAL_DB}\\\`.system_users WHERE username='superadmin';\""

echo "Done"
