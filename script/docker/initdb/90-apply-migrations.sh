#!/usr/bin/env bash
# 全量应用 sql/mysql 目录下的迁移脚本。
# 设计目标：代替手工维护的挂载清单，新增迁移零维护自动生效。
# 已在备份/前序脚本中应用过的语句会报错（表已存在、列重复等），
# 通过 mysql --force 跳过失败语句继续执行，保证可重复执行不中断。
#
# 护栏：统计 "Unknown database" 类错误（历史迁移脚本含 USE 错误库名导致后续段落未执行），
# 若非零则在末尾醒目告警，避免静默丢失种子数据。
set -u

DB="${MYSQL_DATABASE:-weitee-erp}"
MIGRATIONS_DIR="/migrations"
UNKNOWN_DB_TOTAL=0
FILE_COUNT=0

if [ -z "${MYSQL_ROOT_PASSWORD:-}" ]; then
  echo "[migrations] MYSQL_ROOT_PASSWORD is not set, skip."
  return 0 2>/dev/null || exit 0
fi

echo "[migrations] applying all migrations from ${MIGRATIONS_DIR} into ${DB}"

while IFS= read -r f; do
  FILE_COUNT=$((FILE_COUNT + 1))
  FILE_LABEL="${f#${MIGRATIONS_DIR}/}"
  echo "[migrations] >>> ${FILE_LABEL}"

  FILE_OUTPUT=$(mysql --force --default-character-set=utf8mb4 -uroot -p"${MYSQL_ROOT_PASSWORD}" "${DB}" < "${f}" 2>&1)
  UNK_COUNT=$(echo "${FILE_OUTPUT}" | grep -c "Unknown database" || true)

  printf '%s\n' "${FILE_OUTPUT}"

  if [ "${UNK_COUNT}" -gt 0 ]; then
    echo "[migrations] ⚠️  ${FILE_LABEL}: ${UNK_COUNT} 条 Unknown database 错误，该文件中部分段落未执行"
    UNKNOWN_DB_TOTAL=$((UNKNOWN_DB_TOTAL + UNK_COUNT))
  fi
done < <(find "${MIGRATIONS_DIR}" -type f -name '*.sql' ! -path '*/base/*' | sort)

echo "[migrations] all done. processed ${FILE_COUNT} files."
if [ "${UNKNOWN_DB_TOTAL}" -gt 0 ]; then
  echo "[migrations] ⚠️  共检测到 ${UNKNOWN_DB_TOTAL} 条 Unknown database 错误（库名不匹配），已跳过的段落未写入，需人工检查"
fi
