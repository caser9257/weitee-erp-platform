#!/usr/bin/env bash
set -euo pipefail

JAR_PATH="/mnt/d/ruoyi-vue-pro/yudao-server/target/yudao-server.jar"

if [ ! -f "$JAR_PATH" ]; then
  echo "Jar not found: $JAR_PATH" >&2
  exit 1
fi

docker rm -f yudao-server-smoke >/dev/null 2>&1 || true

DB_ARGS="--spring.datasource.dynamic.datasource.master.url=jdbc:mysql://host.docker.internal:3306/ruoyi-vue-pro?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&nullCatalogMeansCurrent=true&rewriteBatchedStatements=true \
--spring.datasource.dynamic.datasource.master.username=root \
--spring.datasource.dynamic.datasource.master.password=123456 \
--spring.datasource.dynamic.datasource.slave.url=jdbc:mysql://host.docker.internal:3306/ruoyi-vue-pro?useSSL=false&serverTimezone=Asia/Shanghai&allowPublicKeyRetrieval=true&nullCatalogMeansCurrent=true&rewriteBatchedStatements=true \
--spring.datasource.dynamic.datasource.slave.username=root \
--spring.datasource.dynamic.datasource.slave.password=123456 \
--spring.redis.host=host.docker.internal \
--spring.redis.port=6379 \
--spring.redis.database=0 \
--server.port=48080"

for i in $(seq 1 30); do
  if docker exec mysql mysql -uroot -p123456 -e "select 1" >/dev/null 2>&1; then
    break
  fi
  sleep 2
done

docker run -d \
  --name yudao-server-smoke \
  --restart unless-stopped \
  -p 48080:48080 \
  --network bridge \
  --add-host host.docker.internal:host-gateway \
  -e SPRING_PROFILES_ACTIVE=local \
  -v "$JAR_PATH:/yudao-server/app.jar:ro" \
  eclipse-temurin:21-jre \
  sh -lc "java -Xms512m -Xmx512m -Djava.security.egd=file:/dev/./urandom -jar /yudao-server/app.jar $DB_ARGS"

for i in $(seq 1 90); do
  if curl -fsS http://127.0.0.1:48080/actuator/health >/tmp/wsl_backend_health.json 2>/tmp/wsl_backend_health.err; then
    cat /tmp/wsl_backend_health.json
    exit 0
  fi
  sleep 2
done

cat /tmp/wsl_backend_health.err 2>/dev/null || true
docker logs --tail 200 yudao-server-smoke 2>&1 || true
exit 1
