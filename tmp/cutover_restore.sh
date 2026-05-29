#!/usr/bin/env bash
set -euo pipefail

MYSQL_ROOT_PASSWORD="${1:?missing mysql root password}"

CONTAINER_NAME="mysql"
OFFICIAL_DB="ruoyi-vue-pro"
RESTORE_DB="ruoyi_vue_pro_restore"
WORKSPACE_TMP="/mnt/d/ruoyi-vue-pro/tmp"
TIMESTAMP="$(date +%Y-%m-%d-%H%M%S)"

CURRENT_BACKUP_PATH="${WORKSPACE_TMP}/ruoyi-vue-pro-before-cutover-${TIMESTAMP}.sql"
RESTORE_SOURCE_DUMP_PATH="${WORKSPACE_TMP}/ruoyi-vue-pro-restore-source-${TIMESTAMP}.sql"
PATCH_SQL_PATH="${WORKSPACE_TMP}/post_cutover_patch.sql"
SALE_USERS_SQL_PATH="${WORKSPACE_TMP}/../sql/mysql/19-erp-sale-order-bpm-test-users.sql"

docker exec "${CONTAINER_NAME}" mysqldump -uroot -p"${MYSQL_ROOT_PASSWORD}" \
  --set-gtid-purged=OFF --single-transaction --routines --events --databases "${OFFICIAL_DB}" \
  > "${CURRENT_BACKUP_PATH}"

docker exec "${CONTAINER_NAME}" mysqldump -uroot -p"${MYSQL_ROOT_PASSWORD}" \
  --set-gtid-purged=OFF --single-transaction --routines --events "${RESTORE_DB}" \
  > "${RESTORE_SOURCE_DUMP_PATH}"

docker exec "${CONTAINER_NAME}" mysql -uroot -p"${MYSQL_ROOT_PASSWORD}" -e \
  "DROP DATABASE IF EXISTS \`${OFFICIAL_DB}\`; CREATE DATABASE \`${OFFICIAL_DB}\` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;"

docker exec -i "${CONTAINER_NAME}" mysql -uroot -p"${MYSQL_ROOT_PASSWORD}" \
  --default-character-set=utf8mb4 --database="${OFFICIAL_DB}" \
  < "${RESTORE_SOURCE_DUMP_PATH}"

docker exec -i "${CONTAINER_NAME}" mysql -uroot -p"${MYSQL_ROOT_PASSWORD}" \
  --default-character-set=utf8mb4 \
  < "${PATCH_SQL_PATH}"

docker exec -i "${CONTAINER_NAME}" mysql --force -uroot -p"${MYSQL_ROOT_PASSWORD}" \
  --default-character-set=utf8mb4 \
  < "${SALE_USERS_SQL_PATH}"

printf 'CURRENT_BACKUP=%s\n' "${CURRENT_BACKUP_PATH}"
printf 'RESTORE_SOURCE_DUMP=%s\n' "${RESTORE_SOURCE_DUMP_PATH}"
