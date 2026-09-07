#!/usr/bin/env bash
#
# 后端联调一键重启脚本（weitee-server）
# 内置四道防线，杜绝"旧代码在跑"：
#   1. 停服后必须确认端口真正释放（防僵尸进程占端口）
#   2. 默认先 install 业务模块到 .m2（单模块 spring-boot:run 从本地仓库取依赖，
#      只 compile 不 install 会跑旧 jar）
#   3. 以日志出现 Spring Boot "Started" 为启动成功标志（端口有响应 != 新代码已上线）
#   4. 启动日志出现 "APPLICATION FAILED TO START" 时立即报错并截取原因（失败是异步暴露的）
#
# 用法：
#   scripts/restart-backend.sh               # 完整流程：停服 → install → 启动 → 等 Started
#   scripts/restart-backend.sh --no-install  # 跳过模块 install（仅当确认依赖未变更时使用）
#
set -u

PORT="${BACKEND_PORT:-48080}"
MODULE="${BACKEND_MODULE:-weitee-module-erp}"
SERVER_MODULE="${BACKEND_SERVER_MODULE:-weitee-server}"
LOG_FILE="${BACKEND_LOG:-${TMPDIR:-/tmp}/opencode/backend-restart.log}"
NO_INSTALL=0
[ "${1:-}" = "--no-install" ] && NO_INSTALL=1

mkdir -p "$(dirname "$LOG_FILE")"

echo "[1/4] 停止旧服务（端口 $PORT）..."
pids=$(netstat -ano 2>/dev/null | grep ":$PORT" | grep -i "LISTENING" | awk '{print $NF}' | sort -u)
if [ -n "$pids" ]; then
  for pid in $pids; do
    echo "    kill 进程树: $pid"
    taskkill //F //T //PID "$pid" > /dev/null 2>&1 || true
  done
else
  echo "    端口空闲，无需停服"
fi

released=0
for i in $(seq 1 10); do
  if ! netstat -ano 2>/dev/null | grep ":$PORT" | grep -qi "LISTENING"; then
    released=1
    break
  fi
  sleep 1
done
if [ "$released" -ne 1 ]; then
  echo "错误：端口 $PORT 在 10 秒后仍被占用，请手动排查：" >&2
  netstat -ano | grep ":$PORT" | grep -i LISTENING >&2
  exit 1
fi
echo "    端口已释放"

if [ "$NO_INSTALL" -ne 1 ]; then
  echo "[2/4] install $MODULE 到本地仓库..."
  if ! mvn -q install -pl "$MODULE" -DskipTests; then
    echo "错误：模块 install 失败，中止启动。" >&2
    exit 1
  fi
else
  echo "[2/4] 跳过 install（--no-install）"
fi

echo "[3/4] 启动 $SERVER_MODULE（日志: $LOG_FILE）..."
: > "$LOG_FILE"
nohup mvn spring-boot:run -pl "$SERVER_MODULE" -DskipTests > "$LOG_FILE" 2>&1 &

echo "[4/4] 等待 Spring Boot 启动完成（最长 300 秒）..."
for i in $(seq 1 60); do
  if grep -q "APPLICATION FAILED TO START\|BUILD FAILURE" "$LOG_FILE" 2>/dev/null; then
    echo "错误：应用启动失败，日志末尾如下：" >&2
    tail -20 "$LOG_FILE" >&2
    exit 1
  fi
  if grep -Eq "Started .+ in [0-9.]+ seconds" "$LOG_FILE" 2>/dev/null; then
    echo ""
    echo "启动成功：http://127.0.0.1:$PORT （耗时约 $((i * 5)) 秒）"
    echo "停止服务：netstat -ano | grep :$PORT 找到 PID 后 taskkill //F //T //PID <pid>"
    exit 0
  fi
  sleep 5
done

echo "错误：300 秒内未见 Started 标志，日志末尾如下：" >&2
tail -20 "$LOG_FILE" >&2
exit 1
