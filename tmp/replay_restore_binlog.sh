#!/usr/bin/env bash
set -euo pipefail

SUDO_PASSWORD="${1:?missing sudo password}"
MYSQL_ROOT_PASSWORD="${2:?missing mysql root password}"
RESTORE_DB="${3:?missing restore db name}"
STOP_POSITION="${4:?missing stop position}"

MYSQL_DATA_DIR="/home/weitai/docker-data/mysql"
BINLOGS=(
  "${MYSQL_DATA_DIR}/binlog.000001"
  "${MYSQL_DATA_DIR}/binlog.000002"
  "${MYSQL_DATA_DIR}/binlog.000003"
  "${MYSQL_DATA_DIR}/binlog.000004"
  "${MYSQL_DATA_DIR}/binlog.000005"
  "${MYSQL_DATA_DIR}/binlog.000006"
  "${MYSQL_DATA_DIR}/binlog.000007"
  "${MYSQL_DATA_DIR}/binlog.000008"
  "${MYSQL_DATA_DIR}/binlog.000009"
)

printf 'SET sql_log_bin=0;\n' > /tmp/replay_restore_binlog.sql
printf '%s\n' "${SUDO_PASSWORD}" | sudo -S mariadb-binlog \
  --database='ruoyi-vue-pro' \
  --rewrite-db='ruoyi-vue-pro->'"${RESTORE_DB}" \
  --stop-position="${STOP_POSITION}" \
  "${BINLOGS[@]}" \
  | sed -E 's/, @@session.check_constraint_checks=1, @@session.system_versioning_insert_history=0//g' \
  | awk '
      BEGIN { skip = 0 }
      /CREATE DATABASE `ruoyi-vue-pro`/ { skip = 1; next }
      /DROP DATABASE IF EXISTS `ruoyi-vue-pro`/ { skip = 1; next }
      skip == 1 && index($0, "/*!*/;") > 0 { skip = 0; next }
      skip == 1 { next }
      { print }
    ' \
  >> /tmp/replay_restore_binlog.sql

cat /tmp/replay_restore_binlog.sql | docker exec -i mysql mysql --force -uroot -p"${MYSQL_ROOT_PASSWORD}" "${RESTORE_DB}"
